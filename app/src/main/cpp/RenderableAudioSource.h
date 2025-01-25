//
// Created by Sam on 1/22/2025.
//

#ifndef METROGNOME_RENDERABLEAUDIOSOURCE_H
#define METROGNOME_RENDERABLEAUDIOSOURCE_H

#include "IRenderableAudio.h"

/**
 * This class renders Float audio, but can be tapped to control.
 * It also contains members for sample rate and channel count
 */
class RenderableAudioSource : public IRenderableAudio {
public:
    RenderableAudioSource(int32_t sampleRate, int32_t channelCount) :
            mSampleRate(sampleRate), mChannelCount(channelCount) { }

    int32_t mSampleRate;
    int32_t mChannelCount;
};

#endif //METROGNOME_RENDERABLEAUDIOSOURCE_H
