package me.c1oky;

public class Loader {

    public static final long START_TIME = System.currentTimeMillis();

    public static void main(String[] args) {
        VkCore vkCore = new VkCore();
        vkCore.start();
    }
}