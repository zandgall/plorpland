/* zandgall

 ## Sound
 # A class that stores an OpenAL sound ID from a given file. Can be controlled with fading and volume

 : MADE IN NEOVIM */

package com.zandgall.plorpland;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_decode_filename;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.libc.LibCStdlib.free;

public class Sound implements Serializable {

	public static final float DEFAULT_SMOOTHING = (1.0f / 16.0f) / (130.0f / 60.f);

	private static String deviceName;
	private static long device, context;

	static {
		deviceName = alcGetString(0, ALC_DEFAULT_DEVICE_SPECIFIER);
		device = alcOpenDevice(deviceName);
		int[] attribs = {0};
		context = alcCreateContext(device, attribs);
		alcMakeContextCurrent(context);
		ALCCapabilities alcCapabilities = ALC.createCapabilities(device);
		ALCapabilities alCapabilities = AL.createCapabilities(alcCapabilities);
	}

	public static Sound
		Noise = new Sound("sound/noise.ogg"), // Constant
		Wind = new Sound("sound/wind.ogg"), // Used for trees
		Piano = new Sound("sound/piano.ogg"),
		EPiano = new Sound("sound/epiano.ogg"),
		Drums = new Sound("sound/drums.ogg"),
		Plorp = new Sound("sound/plorp.ogg"),
		BossDrums = new Sound("sound/bossDrums.ogg"),
		BossEPiano = new Sound("sound/bossEPiano.ogg"),
		BossBass = new Sound("sound/bossBass.ogg"),
		BossGuitar = new Sound("sound/bossGuitar.ogg"),
		BossCymbals = new Sound("sound/bossCymbals.ogg"),
		EndIt = new Sound("sound/endit.ogg"),
		TheKill = new Sound("sound/thekill.ogg"),
		Heaven = new Sound("sound/heaven.ogg"),
		EffectPluck = new Sound("sound/pluck.ogg"),
		EffectBonk = new Sound("sound/bonk.ogg");

	private static double Timer = 0;

	protected int buffer, source, charges = 0;

	protected float volume = 0.0f, targetVolume = 0.0f;
	protected float smoothing = DEFAULT_SMOOTHING;

	public Sound(String filepath) {
		try (MemoryStack stack = stackPush()) {
			IntBuffer channels = stack.mallocInt(1);
			IntBuffer sampleRate = stack.mallocInt(1);
			ShortBuffer rawAudio = stb_vorbis_decode_filename(filepath, channels, sampleRate);
			buffer = alGenBuffers();
			alBufferData(buffer, AL_FORMAT_STEREO16, rawAudio, sampleRate.get());
			free(rawAudio);	

			source = alGenSources();
			alSourcei(source, AL_BUFFER, buffer);
			alSourcei(source, AL_LOOPING, 1);
			alSourcef(source, AL_GAIN, 0.0f);
		}
	}

	public static void init() {
		Noise.fadeTo(1.0f);
		Noise.setVolume(1.0f);
		Piano.fadeTo(1.f);
		Wind.setSmoothing(DEFAULT_SMOOTHING * 16);
		Plorp.setSmoothing(DEFAULT_SMOOTHING * 16);

		Noise.play();
		Wind.play();
		Piano.play();
		EPiano.play();
		Drums.play();
		Plorp.play();
		BossDrums.play();
		BossEPiano.play();
		BossBass.play();
		BossGuitar.play();
		BossCymbals.play();
		EndIt.play();
		Heaven.play();

		TheKill.setVolume(1.f);
		TheKill.fadeTo(1.f);
		TheKill.stopLooping();

		EffectBonk.setVolume(1.f);
		EffectBonk.fadeTo(1.f);
		EffectBonk.stopLooping();
		EffectPluck.setVolume(1.f);
		EffectPluck.fadeTo(1.f);
		EffectPluck.stopLooping();
	}

	public static void kill() {
		Noise.die();
		Wind.die();
		Piano.die();
		EPiano.die();
		Drums.die();
		Plorp.die();
		BossDrums.die();
		BossEPiano.die();
		BossBass.die();
		BossGuitar.die();
		BossCymbals.die();
		EndIt.die();
		Heaven.die();
		TheKill.die();
		EffectBonk.die();
		EffectPluck.die();
		alcDestroyContext(context);
		alcCloseDevice(device);
	}

	private void tick() {
		if(Math.abs(volume - targetVolume) > smoothing * Main.TIMESTEP)
			volume +=smoothing * Main.TIMESTEP * Math.signum(targetVolume - volume);
		alSourcef(source, AL_GAIN, volume);

		double pTimer = Timer;
		Timer += Main.TIMESTEP * 60.f / 130.f;
		if(Math.floor(Timer) != Math.floor(pTimer)) {
			if(EffectBonk.charges > 0) {
				EffectBonk.play();
				EffectBonk.charges--;
			}
			if(EffectPluck.charges > 0) {
				EffectPluck.play();
				EffectPluck.charges--;
			}
		}
	}

	public void fadeTo(float target) {
		targetVolume = target;
	}

	public void setVolume(float volume) {
		this.volume = volume;
		alSourcef(source, AL_GAIN, volume);
	}

	public float getVolume() {
		return volume;
	}

	public void setMinVolume(float minVolume) {
		if(volume < minVolume)
			setVolume(minVolume);
	}

	public void setSmoothing(float smoothing) {
		this.smoothing = smoothing;
	}

	public void play() {
		alSourcePlay(source);
	}

	public void stopLooping() {
		alSourcei(source, AL_LOOPING, 0);
	}

	public void charge() {
		charges++;
	}

	private void die() {
		alDeleteSources(source);
		alDeleteBuffers(buffer);
	}

	public static void update() {
		Noise.tick();
		Wind.tick();
		Piano.tick();
		EPiano.tick();
		Drums.tick();
		Plorp.tick();
		BossDrums.tick();
		BossEPiano.tick();
		BossBass.tick();
		BossGuitar.tick();
		BossCymbals.tick();
		EndIt.tick();
		Heaven.tick();
		TheKill.tick();
	}

	public static void save(ObjectOutputStream out) throws IOException {
		out.writeObject(Noise);
		out.writeObject(Wind);
		out.writeObject(Piano);
		out.writeObject(EPiano);
		out.writeObject(Drums);
		out.writeObject(Plorp);
		out.writeObject(BossDrums);
		out.writeObject(BossEPiano);
		out.writeObject(BossBass);
		out.writeObject(BossGuitar);
		out.writeObject(BossCymbals);
		out.writeObject(EndIt);
		out.writeObject(TheKill);
		out.writeObject(Heaven);
		out.writeObject(EffectPluck);
		out.writeObject(EffectBonk);
	}

	public static void load(ObjectInputStream in) throws IOException {
		try {
			Noise = (Sound)in.readObject();
			Wind = (Sound)in.readObject();
			Piano = (Sound)in.readObject();
			EPiano = (Sound)in.readObject();
			Drums = (Sound)in.readObject();
			Plorp = (Sound)in.readObject();
			BossDrums = (Sound)in.readObject();
			BossEPiano = (Sound)in.readObject();
			BossBass = (Sound)in.readObject();
			BossGuitar = (Sound)in.readObject();
			BossCymbals = (Sound)in.readObject();
			EndIt = (Sound)in.readObject();
			TheKill = (Sound)in.readObject();
			Heaven = (Sound)in.readObject();
			EffectPluck = (Sound)in.readObject();
			EffectBonk = (Sound)in.readObject();
		} catch(ClassNotFoundException e) {
			System.err.println("Couldn't load sound data");
			e.printStackTrace();
		}
	}
}
