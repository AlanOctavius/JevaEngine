/*******************************************************************************
 * Copyright (c) 2013 Jeremy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * If you'd like to obtain a another license to this code, you may contact Jeremy to discuss alternative redistribution options.
 * 
 * Contributors:
 *     Jeremy - initial API and implementation
 ******************************************************************************/
package io.github.jevaengine.audio;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import io.github.jevaengine.Core;
import io.github.jevaengine.IDisposable;
import io.github.jevaengine.ResourceLibrary;
import io.github.jevaengine.ResourceIOException;
import io.github.jevaengine.util.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

public final class Audio
{

	private static final ArrayList<ClipCache> m_clipCaches = new ArrayList<ClipCache>();

	private static final ClipCleanup m_cleanup = new ClipCleanup();

	private AudioLineListener m_lineListener = new AudioLineListener();

	private String m_clipName;

	@Nullable private Clip m_clip;

	@Nullable private ClipCache m_clipOwner;

	public Audio(String name)
	{
		String formal = name.replace("\\", "/").trim().toLowerCase();

		if (formal.startsWith("/"))
			formal = formal.substring(1);

		m_clipName = formal;
		m_clip = null;
	}

	private static void cleanupCache()
	{
		synchronized (m_clipCaches)
		{
			ArrayList<ClipCache> garbageCaches = new ArrayList<ClipCache>();

			for (ClipCache cache : m_clipCaches)
			{
				cache.cleanupCache();

				if (cache.isEmpty())
				{
					cache.dispose();
					garbageCaches.add(cache);
				}
			}

			m_clipCaches.removeAll(garbageCaches);
		}
	}

	private synchronized Clip getClip()
	{
		if (m_clip != null)
			return m_clip;

		synchronized (m_clipCaches)
		{
			for (ClipCache cache : m_clipCaches)
			{
				if (cache.getName().equals(m_clipName))
				{
					m_clipOwner = cache;
					m_clip = cache.getClip();
					m_clip.addLineListener(m_lineListener);
					return m_clip;
				}
			}
		}

		// If we get here there is no clip-cache under m_clipName.
		ClipCache cache = new ClipCache(m_clipName);
		m_clipOwner = cache;
		m_clipCaches.add(cache);

		m_clip = cache.getClip();

		m_clip.addLineListener(m_lineListener);

		return m_clip;
	}

	private synchronized void freeClip()
	{
		m_clip.removeLineListener(m_lineListener);

		m_clipOwner.freeClip(m_clip);
		m_clipOwner = null;
		m_clip = null;
	}

	public void precache()
	{
		getClip();
		freeClip();
	}

	public void play()
	{
		getClip().setFramePosition(0);
		getClip().start();
	}

	public void stop()
	{
		getClip().stop();
	}

	public void repeat()
	{
		getClip().setFramePosition(0);
		getClip().loop(Clip.LOOP_CONTINUOUSLY);
	}

	private class AudioLineListener implements LineListener
	{
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * javax.sound.sampled.LineListener#update(javax.sound.sampled.LineEvent
		 * )
		 */
		@Override
		public void update(LineEvent event)
		{
			if (event.getType() == LineEvent.Type.STOP)
				freeClip();
		}
	}

	private static class ClipCache implements IDisposable
	{

		private ArrayList<SoftReference<Clip>> m_clips = new ArrayList<SoftReference<Clip>>();

		private ArrayList<Clip> m_busyClips = new ArrayList<Clip>();

		private ByteBufferAdapter m_clipStream;

		private String m_clipPath;

		public ClipCache(String path)
		{
			m_clipPath = path;

			InputStream srcStream = Core.getService(ResourceLibrary.class).openAsset(path);

			ByteArrayOutputStream bos = new ByteArrayOutputStream();

			try
			{

				byte[] readBuffer = new byte[2048];

				int length = 0;

				while ((length = srcStream.read(readBuffer, 0, readBuffer.length)) != -1)
				{
					bos.write(readBuffer, 0, length);
				}
				
				srcStream.close();
			} catch (UnsupportedEncodingException e)
			{
				throw new ResourceIOException(e, "Unsupported stream encoding. " + e.getMessage());
			} catch (IOException e)
			{
				throw new ResourceIOException(e, "Error occured while attempting to read file: " + e.getMessage());
			}

			m_clipStream = new ByteBufferAdapter(ByteBuffer.wrap(bos.toByteArray()));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see io.github.jeremywildsmith.jevaengine.IDisposable#dispose()
		 */
		@Override
		public void dispose()
		{
			try
			{
				m_clipStream.close();
			} catch (IOException e)
			{
				// This should never happen
				throw new RuntimeException(e);
			}
		}

		public Clip getClip()
		{
			Clip clip = null;

			ArrayList<SoftReference<Clip>> garbageClips = new ArrayList<SoftReference<Clip>>();

			for (SoftReference<Clip> entry : m_clips)
			{
				if (entry.get() == null)
					garbageClips.add(entry);
				else if (!m_busyClips.contains(entry.get()))
					clip = entry.get();
			}

			m_clips.removeAll(garbageClips);

			if (clip == null)
			{
				try
				{
					m_clipStream.reset();
					try (AudioInputStream ais = AudioSystem.getAudioInputStream(m_clipStream))
					{
						AudioFormat baseFormat = ais.getFormat();
						
						AudioFormat[] supportedTargets = AudioSystem.getTargetFormats(AudioFormat.Encoding.PCM_SIGNED, baseFormat);
						
						if(supportedTargets.length == 0)
							throw new AudioException("No supported target formats found.");
						
						clip = AudioSystem.getClip();
						try (AudioInputStream targetAis = AudioSystem.getAudioInputStream(supportedTargets[0], ais))
						{
							clip.open(targetAis);
						}
					}

					m_clips.add(new SoftReference<Clip>(clip, m_cleanup.getCleanupQueue()));

				} catch (UnsupportedAudioFileException e)
				{
					throw new AudioException("IO Error when loading " + m_clipPath + " " + e.toString());
				} catch (IOException e)
				{
					throw new AudioException("Unsupported Audio Format when loading " + m_clipPath + ", " + e.getMessage());
				} catch (LineUnavailableException e)
				{
					throw new AudioException("No accessible line: " + e.toString());
				}
			}

			m_busyClips.add(clip);

			return clip;
		}

		public void freeClip(Clip clip)
		{
			m_busyClips.remove(clip);
		}

		public void cleanupCache()
		{
			for (SoftReference<Clip> clip : m_clips)
			{
				if (clip.get() == null)
					m_clips.remove(clip);
			}
		}

		public String getName()
		{
			return m_clipPath;
		}

		public boolean isEmpty()
		{
			return m_clips.isEmpty();
		}

		private static class ByteBufferAdapter extends InputStream
		{

			private ByteBuffer m_buffer;

			public ByteBufferAdapter(ByteBuffer buffer)
			{
				m_buffer = buffer;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see java.io.InputStream#reset()
			 */
			@Override
			public synchronized void reset()
			{
				m_buffer.rewind();
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see java.io.InputStream#read()
			 */
			@Override
			public int read() throws IOException
			{
				if (!m_buffer.hasRemaining())
					return -1;

				// And with 0xFF to mask just one byte of data from the read
				// operation.
				return m_buffer.get() & 0xFF;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see java.io.InputStream#read(byte[], int, int)
			 */
			public int read(byte[] bytes, int off, int len) throws IOException
			{
				if (!m_buffer.hasRemaining())
					return -1;

				len = Math.min(len, m_buffer.remaining());
				m_buffer.get(bytes, off, len);
				return len;
			}

		}
	}

	private static class ClipCleanup extends Thread
	{

		private final ReferenceQueue<Clip> m_referenceQueue = new ReferenceQueue<Clip>();

		public ClipCleanup()
		{
			this.setDaemon(true);
			this.start();
		}

		public ReferenceQueue<Clip> getCleanupQueue()
		{
			return m_referenceQueue;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run()
		{
			while (true)
			{
				try
				{
					m_referenceQueue.remove().get().close();
					cleanupCache();
				} catch (InterruptedException e)
				{
					Thread.currentThread().interrupt();
				}
			}
		}
	}
}
