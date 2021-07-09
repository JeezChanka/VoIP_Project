package pl.poznan.put.voip.client.utils;


public interface Controller  {
    default void onResponse(String command, String... args) {}
}
