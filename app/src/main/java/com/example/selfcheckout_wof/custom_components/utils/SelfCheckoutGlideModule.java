package com.example.selfcheckout_wof.custom_components.utils;

import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;

/**
 * This class exists solely to avoid the following warning message:
 * Failed to find GeneratedAppGlideModule. You should include an annotationProcessor compile dependency on com.github.bumptech.glide:compiler in your application and a @GlideModule annotated AppGlideModule implementation or LibraryGlideModules will be silently ignored
 *
 * This is a fix according to: https://stackoverflow.com/questions/49901629/glide-showing-error-failed-to-find-generatedappglidemodule
 */
@GlideModule
public class SelfCheckoutGlideModule extends AppGlideModule {
}
