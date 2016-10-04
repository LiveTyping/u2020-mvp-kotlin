package ru.ltst.u2020mvp

import com.f2prateek.rx.preferences.Preference
import ru.ltst.u2020mvp.data.*
import ru.ltst.u2020mvp.ui.debug.DebugView

interface DebugInternalU2020Graph : InternalU2020Graph {
    fun inject(view: DebugView)
    @IsInstrumentationTest fun isInstrumentationTest() : Boolean
    @NetworkDelay fun networkDelay() : Preference<Long>
    @ApiEndpoint fun apiEndpoint() : Preference<String>
    @CaptureIntents fun captureIntents() : Preference<Boolean>
    @AnimationSpeed fun animationSpeed() : Preference<Int>
    @PicassoDebugging fun picassoDebugging() : Preference<Boolean>
    @PixelGridEnabled fun pixelGridEnabled() : Preference<Boolean>
    @PixelRatioEnabled fun pixelRatioEnabled() : Preference<Boolean>
    @ScalpelEnabled fun scalpelEnabled() : Preference<Boolean>
    @ScalpelWireframeEnabled fun scalpelWireframeEnabled() : Preference<Boolean>
    @NetworkFailurePercent fun networkFailurePercent() : Preference<Int>
    @NetworkVariancePercent fun networkVariancePercent() : Preference<Int>
    @IsMockMode fun isMockMode() : Boolean
}
