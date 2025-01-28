//
// Created by Sam on 1/19/2025.
//

#ifndef METROGNOME_METROGNOME_H
#define METROGNOME_METROGNOME_H
#include <oboe/Oboe.h>
#include <array>
#include <vector>
#include <jni.h>
#include "RenderableAudioSource.h"
#include "DefaultDataCallback.h"
#include "DefaultErrorCallback.h"
#include "Oscillator.h"
#include "Mixer.h"
#include "MonoToStereo.h"
constexpr float kOscBaseFrequency = 440.0;
constexpr float kOscAmplitude = 0.9;
enum EighthNoteGrouping {
    DUPLE = 2,
    TRIPLE = 3
};

enum BeatType {
    SILENT,
    STRONG_ACCENT,
    ACCENT,
    UNACCENT
};
struct TimeSignature {
    int top;
    int bottom;
};
struct MeasureData {
    TimeSignature timeSignature;
    std::vector<EighthNoteGrouping> eighthNoteGrouping;
    bool voiceEighths;
    void (*beatTick)(int);
};

class CountdownOscillator {
public:
    void init(int sampleRate, float frequency, float amplitude, Mixer *mixer) {
        mOsc.setSampleRate(sampleRate);
        mOsc.setFrequency(frequency);
        mOsc.setAmplitude(amplitude);
        mixer->addTrack(&mOsc);
    }
    void tick(int numFrames) {
        if (countdown <= 0) {
            countdown = 0;
            return;
        }
        countdown -= numFrames;
        if (countdown <= 0) {
            countdown = 0;
            mOsc.setWaveOn(false);
        }
    }
    void play(int numFrames) {
        countdown = numFrames;
        mOsc.setWaveOn(true);
    }
private:
    Oscillator mOsc;
    int countdown = 0;
};
class Measure {
public:
    TimeSignature mTimeSignature = {7, 8};
    Measure() : currentBeat(0), mAccentOsc(nullptr), mUnaccentOsc(nullptr), mStrongAccentOsc(nullptr), mTimeSignature(TimeSignature{7, 8}){}
    void init(TimeSignature timeSignature, std::vector<EighthNoteGrouping> eighthNoteGrouping, bool voiceEighths, CountdownOscillator *accentOsc, CountdownOscillator *unaccentOsc, CountdownOscillator *strongAccentOsc){
        mTimeSignature = timeSignature;
        mAccentOsc = accentOsc;
        mUnaccentOsc = unaccentOsc;
        mStrongAccentOsc = strongAccentOsc;
        if (timeSignature.bottom == 4) {
            BeatType upBeat = voiceEighths ? UNACCENT : SILENT;
            mAccentPattern.push_back(STRONG_ACCENT);
            mAccentPattern.push_back(upBeat);
            for (int i = 1; i < timeSignature.top; i++) {
                mAccentPattern.push_back(ACCENT);
                mAccentPattern.push_back(upBeat);
            }
        } else {
            mAccentPattern.push_back(STRONG_ACCENT);
            mAccentPattern.push_back(UNACCENT);
            if (!eighthNoteGrouping.empty() && eighthNoteGrouping[0] == TRIPLE) {
                mAccentPattern.push_back(UNACCENT);
            }
            for (int i = 1; i < eighthNoteGrouping.size(); i++) {

                mAccentPattern.push_back(ACCENT);
                mAccentPattern.push_back(UNACCENT);
                if (eighthNoteGrouping[i] == TRIPLE) {
                    mAccentPattern.push_back(UNACCENT);
                }
            }
            while (mAccentPattern.size() < mTimeSignature.top) {
                mAccentPattern.push_back(UNACCENT);
            }
        }
        currentBeat = 0;
    }
    bool eigthNoteBeat() {
        BeatType nextBeat = mAccentPattern[currentBeat++];
        if (nextBeat == STRONG_ACCENT) {
            mStrongAccentOsc->play(1500);
        } else if (nextBeat == ACCENT) {
            mAccentOsc->play(1500);

        } else if (nextBeat == UNACCENT) {
            mUnaccentOsc->play(1500);
        }
        if (currentBeat >= (mTimeSignature.top * 8 / mTimeSignature.bottom) ) {
            currentBeat = 0;
            return true;
        } else {
            return false;
        }
    }

    int currentBeat;
private:
    CountdownOscillator *mStrongAccentOsc;
    CountdownOscillator *mAccentOsc;
    CountdownOscillator *mUnaccentOsc;
    std::vector<BeatType> mAccentPattern;
};
class Synth : public RenderableAudioSource {
public:

    std::vector<Measure> mMeasures;
    void setBpm(int bpm) {
        float secondsPerBeat = 60.0f / (float)bpm;
        beatDelayFrames = secondsPerBeat * mSampleRate;
    }
    Synth(int32_t sampleRate, int32_t channelCount) :
            RenderableAudioSource(sampleRate, channelCount) {
        strongAccentOsc.init(sampleRate, kOscBaseFrequency * 2, kOscAmplitude, &mMixer);
        accentOsc.init(sampleRate, kOscBaseFrequency, kOscAmplitude * 0.8, &mMixer);
        unaccentOsc.init(sampleRate, kOscBaseFrequency, kOscAmplitude*0.4, &mMixer);
        if (mChannelCount == oboe::ChannelCount::Stereo) {
            mOutputStage =  &mConverter;
        } else {
            mOutputStage = &mMixer;
        }
        currentMeasure = 0;
    }
    Measure createMeasure(const MeasureData& measureData) {
        Measure m;
        m.init(measureData.timeSignature, measureData.eighthNoteGrouping, measureData.voiceEighths, &accentOsc, &unaccentOsc, &strongAccentOsc);
        return m;
    }
    // From IRenderableAudio
    void renderAudio(float *audioData, int32_t numFrames) override {
        currentFrame += numFrames;
        strongAccentOsc.tick(numFrames);
        accentOsc.tick(numFrames);
        unaccentOsc.tick(numFrames);
        if (mMeasures.empty()) {
            return;
        }
        if (currentFrame >= beatDelayFrames/2) {
            currentFrame %= (beatDelayFrames/2);
            beatTick(currentMeasure, mMeasures[currentMeasure].currentBeat);
            bool completedMeasure = mMeasures[currentMeasure].eigthNoteBeat();
            if (completedMeasure) {
                currentMeasure = (currentMeasure + 1) % (int)mMeasures.size();
            }
        }
        mOutputStage->renderAudio(audioData, numFrames);

    };
    void beatTick(int measure, int beat) {
        JNIEnv* env;
        jvm->AttachCurrentThread(&env, nullptr); // Attach the current thread to the JVM
        jclass callbackClass = env->GetObjectClass(metronomeCallback);
        jmethodID onMetronomeClickMethod = env->GetMethodID(callbackClass, "onMetronomeClick", "(II)V");
        env->CallVoidMethod(metronomeCallback, onMetronomeClickMethod, measure, beat);
        jvm->DetachCurrentThread(); //
    }
    JavaVM* jvm;
    jobject metronomeCallback;
    ~Synth() override = default;
private:
    // Rendering objects
    CountdownOscillator accentOsc;
    CountdownOscillator strongAccentOsc;
    CountdownOscillator unaccentOsc;
    Mixer mMixer;
    MonoToStereo mConverter = MonoToStereo(&mMixer);
    IRenderableAudio *mOutputStage; // This will point to either the mixer or converter, so it needs to be raw
    int currentMeasure = 0;
    int currentFrame = 0;
    int beatDelayFrames = kDefaultSampleRate;
};
class MetronomeNoiseMaker:public IRestartable {
public:

    /**
     * Open an Oboe stream.
     * @return OK or negative error code.
     */
    oboe::Result open();

    oboe::Result start();

    oboe::Result stop();

    oboe::Result close();
    void setBpm(int bpm);
    void setMeasures(const std::vector<MeasureData>& measures);
    void setMetronomeCallback(JavaVM* jvm, jobject metronomeCallback);
    void restart() override;

private:



    std::shared_ptr<oboe::AudioStream> mStream;
    std::shared_ptr<Synth> mAudioSource;
    std::shared_ptr<DefaultDataCallback> mDataCallback;
    std::shared_ptr<DefaultErrorCallback> mErrorCallback;

};


#endif //METROGNOME_METROGNOME_H
