package com.reprojector.helpers;

public class Utils {
    public static long usableMemory() {
        try {
            return  Runtime.getRuntime().maxMemory()
                    - (Runtime.getRuntime().totalMemory()
                    - Runtime.getRuntime().freeMemory());
        } catch (ArithmeticException exception){
          return Integer.MAX_VALUE;
        }
    }
}
