package com.dtiguardian.commport;

public class Stopwatch {
    private volatile long start;
    private volatile long count;
    private volatile boolean running;

    public Stopwatch()
    {
        start = 0;
        count = 0;
        running = false;
    }

    public Stopwatch(boolean startStopwatch)
    {
        if(startStopwatch)
        {
            count = 0;
            start = System.nanoTime();
            running = true;
        }
    }

    public long Read()
    {
        if(running)
        {
            long snapshot = System.nanoTime();
            return((snapshot-start)/1000000);
        }
        return(count);
    }

    public void Reset()
    {
        Stop();
        count=0;
    }

    public boolean Compare(long period)
    {
        if(running)
        {
            if(Read()>=period)
            {
                Restart();
                return(true);
            }
        }
        return false;
    }

    public void Stop()
    {
        if(running)
        {
            count+=Read();
            running=false;
        }
    }

    public void Start()
    {
        if(!running)
        {
            start = System.nanoTime();
            running = true;
        }
    }

    public void Restart()
    {
        start = System.nanoTime();
        count = 0;
    }

    public boolean IsRunning()
    {
        return running;
    }
}
