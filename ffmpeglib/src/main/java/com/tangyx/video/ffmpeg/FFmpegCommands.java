package com.tangyx.video.ffmpeg;

import android.util.Log;


public class FFmpegCommands {


    /**
     * 视频  及 音频 反转
     *
     * @param videoUrl
     * @param outUrl
     * @return
     */
    public static String[] reverseAudioAndVideo(String videoUrl, String outUrl) {
        // For video only
//        ffmpeg -i input-file.mp4 -vf reverse output.mp4

        // For audio and video:
//        ffmpeg -i input-file.mp4 -vf reverse -af areverse output.mp4

//        –s 352*278

        String[] commands = new String[10];
        commands[0] = "ffmpeg";
        commands[1] = "-i";
        commands[2] = videoUrl;
        commands[3] = "-vf";
        commands[4] = "reverse";
        commands[5] = "-af";
        commands[6] = "areverse";
        commands[7] = "-q";
        commands[8] = "0";
        commands[9] = outUrl;
        return commands;
    }


    /**
     * 仅仅视频反转
     *
     * @param videoUrl
     * @param outUrl
     * @return
     */
    public static String[] reverseVideo(String videoUrl, String outUrl) {

        // For video only
//        ffmpeg -i input-file.mp4 -vf reverse output.mp4

        String[] commands = new String[6];
        commands[0] = "ffmpeg";
        commands[1] = "-i";
        commands[2] = videoUrl;
        commands[3] = "-vf";
        commands[4] = "reverse";
        commands[5] = outUrl;
        return commands;
    }

    /**
     * 提取单独的音频
     *
     * @param videoUrl
     * @param outUrl
     * @return
     */
    public static String[] extractAudio(String videoUrl, String outUrl) {
        String[] commands = new String[8];
        commands[0] = "ffmpeg";
        commands[1] = "-i";
        commands[2] = videoUrl;
        commands[3] = "-acodec";
        commands[4] = "copy";
        commands[5] = "-vn";
        commands[6] = "-y";
        commands[7] = outUrl;
        return commands;
    }


    /**
     * 提取单独的视频，没有声音
     *
     * @param videoUrl
     * @param outUrl
     * @return
     */
    public static String[] extractVideo(String videoUrl, String outUrl) {
        String[] commands = new String[8];
        commands[0] = "ffmpeg";
        commands[1] = "-i";
        commands[2] = videoUrl;
        commands[3] = "-vcodec";
        commands[4] = "copy";
        commands[5] = "-an";
        commands[6] = "-y";
        commands[7] = outUrl;
        return commands;
    }

    /**
     * 裁剪音频
     */
    public static String[] cutIntoMusic(String musicUrl, long second, String outUrl) {
        Log.e("SLog", musicUrl + "---" + second + "---" + outUrl);
        String[] commands = new String[10];
        commands[0] = "ffmpeg";
        commands[1] = "-i";
        commands[2] = musicUrl;
        commands[3] = "-ss";
        commands[4] = "00:00:10";
        commands[5] = "-t";
        commands[6] = String.valueOf(second);
        commands[7] = "-acodec";
        commands[8] = "copy";
        commands[9] = outUrl;
        return commands;
    }

    /**
     * 两段音频合成
     *
     * @param audio1
     * @param audio2
     * @param outputUrl
     * @return
     */
    public static String[] composeAudio(String audio1, String audio2, String outputUrl) {
        Log.w("SLog", "audio1:" + audio1 + "\naudio2:" + audio2 + "\noutputUrl:" + outputUrl);
        String[] commands = new String[10];
        commands[0] = "ffmpeg";
        //输入
        commands[1] = "-i";
        commands[2] = audio1;
        //音乐
        commands[3] = "-i";
        commands[4] = audio2;
        //覆盖输出
        commands[5] = "-filter_complex";
        commands[6] = "amix=inputs=2:duration=first:dropout_transition=2";
        commands[7] = "-strict";
        commands[8] = "-2";
        //输出文件
        commands[9] = outputUrl;
        return commands;
    }

    /**
     * 修改音频文件的音量
     *
     * @param audioOrMusicUrl
     * @param vol
     * @param outUrl
     * @return
     */
    public static String[] changeAudioOrMusicVol(String audioOrMusicUrl, int vol, String outUrl) {
        Log.w("SLog", "audioOrMusicUrl:" + audioOrMusicUrl + "\nvol:" + vol + "\noutUrl:" + outUrl);
        String[] commands = new String[8];
        commands[0] = "ffmpeg";
        commands[1] = "-i";
        commands[2] = audioOrMusicUrl;
        commands[3] = "-vol";
        commands[4] = String.valueOf(vol);
        commands[5] = "-acodec";
        commands[6] = "copy";
        commands[7] = outUrl;
        return commands;
    }

    /**
     * 音频，视频合成
     *
     * @param videoUrl
     * @param musicOrAudio
     * @param outputUrl
     * @param second
     * @return
     */
    public static String[] composeVideo(String videoUrl, String musicOrAudio, String outputUrl, long second) {
        Log.w("SLog", "videoUrl:" + videoUrl + "\nmusicOrAudio:" + musicOrAudio + "\noutputUrl:" + outputUrl + "\nsecond:" + second);
        String[] commands = new String[14];
        commands[0] = "ffmpeg";
        //输入
        commands[1] = "-i";
        commands[2] = videoUrl;
        //音乐
        commands[3] = "-i";
        commands[4] = musicOrAudio;
        commands[5] = "-ss";
        commands[6] = "00:00:00";
        commands[7] = "-t";
        commands[8] = String.valueOf(second);
        //覆盖输出
        commands[9] = "-vcodec";
        commands[10] = "copy";
        commands[11] = "-acodec";
        commands[12] = "copy";
        //输出文件
        commands[13] = outputUrl;
        return commands;
    }


    /**
     * 音频，视频合成
     *
     * @param videoUrl
     * @param musicOrAudio
     * @param outputUrl
     * @return
     */
    public static String[] composeVideoAndAudio(String videoUrl, String musicOrAudio, String outputUrl) {
        Log.w("SLog", "videoUrl:" + videoUrl + "\nmusicOrAudio:" + musicOrAudio + "\noutputUrl:" + outputUrl);
        String[] commands = new String[14];
        commands[0] = "ffmpeg";
        //输入
        commands[1] = "-i";
        commands[2] = videoUrl;
        //音乐
        commands[3] = "-i";
        commands[4] = musicOrAudio;

        //覆盖输出
        commands[9] = "-vcodec";
        commands[10] = "copy";
        commands[11] = "-acodec";
        commands[12] = "copy";
        //输出文件
        commands[13] = outputUrl;
        return commands;
    }
}
