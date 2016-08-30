package org.opencv.android;

import org.opencv.core.Core;

import java.util.StringTokenizer;

import timber.log.Timber;

class StaticHelper {

    public static boolean initOpenCV(boolean InitCuda)
    {
        boolean result;
        String libs = "";

        if(InitCuda)
        {
            loadLibrary("cudart");
            loadLibrary("nppc");
            loadLibrary("nppi");
            loadLibrary("npps");
            loadLibrary("cufft");
            loadLibrary("cublas");
        }

        Timber.d("Trying to get library list");

        try
        {
            System.loadLibrary("opencv_info");
            libs = getLibraryList();
        }
        catch(UnsatisfiedLinkError e)
        {
            Timber.e("OpenCV error: Cannot load info library for OpenCV");
        }

        Timber.d("Library list: \"" + libs + "\"");
        Timber.d("First attempt to load libs");
        if (initOpenCVLibs(libs))
        {
            Timber.d("First attempt to load libs is OK");
            String eol = System.getProperty("line.separator");
            for (String str : Core.getBuildInformation().split(eol))
                Timber.i(str);

            result = true;
        }
        else
        {
            Timber.d("First attempt to load libs fails");
            result = false;
        }

        return result;
    }

    private static boolean loadLibrary(String Name)
    {
        boolean result = true;

        Timber.d("Trying to load library " + Name);
        try
        {
            System.loadLibrary(Name);
            Timber.d("Library " + Name + " loaded");
        }
        catch(UnsatisfiedLinkError e)
        {
            Timber.d("Cannot load library \"" + Name + "\"");
            e.printStackTrace();
            result &= false;
        }

        return result;
    }

    private static boolean initOpenCVLibs(String Libs)
    {
        Timber.d("Trying to init OpenCV libs");

        boolean result = true;

        if ((null != Libs) && (Libs.length() != 0))
        {
            Timber.d("Trying to load libs by dependency list");
            StringTokenizer splitter = new StringTokenizer(Libs, ";");
            while(splitter.hasMoreTokens())
            {
                result &= loadLibrary(splitter.nextToken());
            }
        }
        else
        {
            // If dependencies list is not defined or empty.
            result &= loadLibrary("opencv_java3");
        }

        return result;
    }

    private static native String getLibraryList();
}
