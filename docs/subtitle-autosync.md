# Subtitle AutoSync

Subtitle AutoSync compares an add-on subtitle with subtitle timing embedded in the
playing video. Confident results can correct a constant delay, frame-rate drift,
or the timing of individual cue groups. Failed analysis keeps the selected subtitle
and its timing.

When the video has no embedded subtitle timing, Morivia lines the subtitle up with
the speech in the audio instead (see "Speech sync" below).

Enable **Auto Sync Subtitle** in **Settings → Playback → Subtitle / Audio** after
choosing a preferred subtitle language. The switch is on by default. Its tolerance
and Thorough/Quick search controls appear only while the switch is on. Tolerance
keeps the original timing when the whole-film correction is within the chosen
100–500 ms limit; Off applies all confident corrections.

AutoSync works with HTTP(S) streams in ExoPlayer and libmpv on Android and in libmpv
on iPhone and iPad. ExoPlayer applies results to the subtitles its sidecar renderer
supports; ASS/SSA selected with libass and external players keep their own subtitle
behavior. Videos without usable embedded subtitle timing use speech sync.

At startup, AutoSync can try other add-on subtitles in the same language. When the
user selects a subtitle manually, it checks only that subtitle. An automatic
replacement updates the subtitle selection but does not persist an automatic pick
as a new manual preference for later episodes.

Success is indicated by **Auto synced** on the selected subtitle card. Only failures
show toasts. Changing subtitles, selecting an embedded track, disabling subtitles,
manually adjusting delay, or leaving playback cancels pending analysis. Index
requests start only after an add-on subtitle is selected for AutoSync. Applying a
result swaps sidecar cues without reloading the media source or discarding its buffer.

AutoSync has no debug logging setting and does not emit diagnostic logs.

## Sources

- Mobile integration and retimer:
  [NuvioMobile-Reshaped](https://github.com/DavidVamaiotu/NuvioMobile-Reshaped/tree/5332f76a).
- Reviewed matcher, parser, sampled-reference handling, and UX behavior:
  [NuvioTV PR #3703](https://github.com/NuvioMedia/NuvioTV/pull/3703), head `613dbd2d`.
- The extractor rewrapping preserves upstream libass handling underneath AutoSync's
  observation wrapper. Existing parsers, general player preferences, dependencies,
  manifests, branding, and release workflows are kept intact.

## Verification

Local validation covered offsets, drift, grouping, confidence rejection, tolerance,
dense dialogue, reference consistency, no-fit tracking, and subtitle formats. Local
network tests covered subtitle HTTP cancellation, shared index requests, and retrying
an index after its owning request is cancelled. The test files are kept outside
the PR at the author’s request.

Physical-device checks still required:

1. With AutoSync off, verify ordinary subtitle selection and manual delay on Android
   and iOS; verify libmpv and external-player behavior.
2. Enable AutoSync and select an offset or drifting subtitle against an MKV or MP4
   with embedded subtitles. Verify the badge appears without a success toast, and
   playback continues without rebuffering.
3. Check an already-aligned subtitle and a correction inside the selected tolerance.
4. Start analysis, then switch to an embedded subtitle, disable subtitles, choose
   another add-on subtitle, adjust delay, or leave the player. The old result must
   not return or overwrite the new choice.
5. Test a video without embedded subtitles and unsupported ASS/libass rendering.
   Verify a simple failure toast and the original timing.
6. Capture the settings rows and selected subtitle badge for PR review.

## Speech sync (Morivia)

When a video has no embedded subtitle timing, AutoSync measures the speech in the
audio that is playing and lines the selected add-on subtitle up with it. It works in
every player Morivia controls: ExoPlayer and libmpv on Android, and libmpv on iPhone
and iPad. External players are not covered.

- **Measuring.** ExoPlayer runs a pass-through audio processor
  (`SpeechTapAudioProcessor`) that measures the voice band (250–3,500 Hz) of every
  20 ms of audio. libmpv runs FFmpeg's `astats` and `ametadata` filters
  (`MpvSpeechFilter`) on a voice-band copy of the audio and writes each frame's level
  with its media time to a cache file, which `MpvSpeechMonitor` reads. The filter is
  added after the file has loaded; libmpv keeps the old audio chain if it cannot
  start, so the sound is never affected. Levels are kept per source in
  `SpeechActivityTimeline`.
- **Matching.** After two minutes of played audio, `AudioSubtitleAligner` turns the
  levels into a speech pattern with a threshold that adapts to the film's mix, then
  tries every shift within ±90 s for each common frame-rate ratio (23.976, 24 and
  25 fps). The best combination is applied only when it stands clearly above every
  other shift; it is retried as more audio arrives, for up to 20 minutes of audio.
- **Applying.** A confident result is a linear transform `new = scale × old + shift`
  and goes through the same apply path as other AutoSync results, so the subtitle
  card shows **Auto synced**. A subtitle that is already within 120 ms is left alone.
- **Following the whole film (1.6.0).** After the first result, and also when the
  whole-film match found nothing or the subtitle was already in sync,
  `AudioSubtitleSync.follow` keeps listening. Every minute of newly played audio it
  lines up the cues of the last four minutes with the speech (±15 s, no frame-rate
  change). Every cue counts, voices and song lyrics (♪) alike. When the same drift is
  measured twice in a row, a minute apart, every cue from the start of that window
  onwards moves by it; cues already shown keep their timing. This corrects subtitles
  for a release where a scene is cut differently. The players apply each correction
  through the same path as the first result (`AutoSyncResolvedTimeline.followSpeech`).

The matcher and the follow-up are covered by `AudioSubtitleAlignerTest` with synthetic
audio. They have not been measured against a wide range of real films; music-heavy
scenes can still leave a subtitle unsynced.
