// Write C++ code here.
//
// Do not forget to dynamically load the C++ library into your application.
//
// For instance,
//
// In MainActivity.java:
//    static {
//       System.loadLibrary("metrognome");
//    }
//
// Or, in MainActivity.kt:
//    companion object {
//      init {
//         System.loadLibrary("metrognome")
//      }
//    }

#include <cstdlib>

static const char *TAG = "Metrognome";

#include <android/log.h>
#include "DefaultDataCallback.h"
#include "DefaultErrorCallback.h"
#include "metrognome.h"

using namespace oboe;

oboe::Result MetronomeNoiseMaker::open() {
    // Use shared_ptr to prevent use of a deleted callback.
    mDataCallback = std::make_shared<DefaultDataCallback>();
    mErrorCallback = std::make_shared<DefaultErrorCallback>(*this);

    AudioStreamBuilder builder;
    oboe::Result result = builder.setSharingMode(oboe::SharingMode::Exclusive)
            ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
            ->setFormat(oboe::AudioFormat::Float)
            ->setDataCallback(mDataCallback)
            ->setErrorCallback(mErrorCallback)
                    // Open using a shared_ptr.
            ->openStream(mStream);
    return result;
}

oboe::Result MetronomeNoiseMaker::start() {
    oboe::Result result = mStream->requestStart();
    if (result == Result::OK) {
        mAudioSource = std::make_shared<Synth>(mStream->getSampleRate(), mStream->getChannelCount());
        mDataCallback->reset();
        mDataCallback->setSource(std::dynamic_pointer_cast<IRenderableAudio>(mAudioSource));
        result = mStream->start();
    }
    return result;
}
oboe::Result MetronomeNoiseMaker::stop() {
    return mStream->requestStop();
}

oboe::Result MetronomeNoiseMaker::close() {
    return mStream->close();
}

void MetronomeNoiseMaker::setBpm(int bpm) {
    mAudioSource->setBpm(bpm);
}
void MetronomeNoiseMaker::setMeasures(const std::vector<MeasureData>& measures) {
 std::vector<Measure> measureObjects;
 measureObjects.reserve(measures.size());
 for (const auto & measure : measures) {
     measureObjects.push_back(mAudioSource->createMeasure(measure));
 }
 mAudioSource->mMeasures = measureObjects;
}
void MetronomeNoiseMaker::restart() {
    stop();
    start();
}
