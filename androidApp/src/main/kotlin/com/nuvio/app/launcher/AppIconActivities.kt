package com.nuvio.app.launcher

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.nuvio.app.MainActivity

/**
 * A launcher entry (one per app icon). It only opens MainActivity in the app's own task and
 * closes, so turning an entry off to change the icon never closes the running app.
 */
open class LauncherEntryActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(
            Intent(intent).apply {
                setClass(this@LauncherEntryActivity, MainActivity::class.java)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            },
        )
        finish()
        overridePendingTransition(0, 0)
    }
}

class AppIconDefault : LauncherEntryActivity()

class AppIconArcticBlue : LauncherEntryActivity()

class AppIconEmerald : LauncherEntryActivity()

class AppIconRoseGold : LauncherEntryActivity()

class AppIconCopper : LauncherEntryActivity()

class AppIconGraphite : LauncherEntryActivity()

class AppIconGold : LauncherEntryActivity()

class AppIconJade : LauncherEntryActivity()

class AppIconCrimson : LauncherEntryActivity()

class AppIconOcean : LauncherEntryActivity()

class AppIconViolet : LauncherEntryActivity()

class AppIconAmber : LauncherEntryActivity()

class AppIconRose : LauncherEntryActivity()

class AppIconWhite : LauncherEntryActivity()
class AppIconAmethyst : LauncherEntryActivity()
class AppIconApricot : LauncherEntryActivity()
class AppIconAubergine : LauncherEntryActivity()
class AppIconAzure : LauncherEntryActivity()
class AppIconBasil : LauncherEntryActivity()
class AppIconBerry : LauncherEntryActivity()
class AppIconBlush : LauncherEntryActivity()
class AppIconCerulean : LauncherEntryActivity()
class AppIconCherry : LauncherEntryActivity()
class AppIconCobalt : LauncherEntryActivity()
class AppIconCoral : LauncherEntryActivity()
class AppIconDenim : LauncherEntryActivity()
class AppIconFern : LauncherEntryActivity()
class AppIconGlacier : LauncherEntryActivity()
class AppIconGrape : LauncherEntryActivity()
class AppIconHeather : LauncherEntryActivity()
class AppIconIndigo : LauncherEntryActivity()
class AppIconLagoon : LauncherEntryActivity()
class AppIconLavender : LauncherEntryActivity()
class AppIconLemon : LauncherEntryActivity()
class AppIconLilac : LauncherEntryActivity()
class AppIconLime : LauncherEntryActivity()
class AppIconMango : LauncherEntryActivity()
class AppIconMint : LauncherEntryActivity()
class AppIconMoss : LauncherEntryActivity()
class AppIconOrchid : LauncherEntryActivity()
class AppIconPeach : LauncherEntryActivity()
class AppIconPeriwinkle : LauncherEntryActivity()
class AppIconPine : LauncherEntryActivity()
class AppIconSaffron : LauncherEntryActivity()
class AppIconTeal : LauncherEntryActivity()
