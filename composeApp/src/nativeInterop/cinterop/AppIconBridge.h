#pragma once

#include <stdbool.h>

typedef void (*MoriviaAppIconCompletion)(bool);

bool MoriviaSupportsAlternateAppIcons(void);
bool MoriviaIsCurrentAlternateAppIcon(const char *name);
void MoriviaSetAlternateAppIconName(const char *name, MoriviaAppIconCompletion completion);
