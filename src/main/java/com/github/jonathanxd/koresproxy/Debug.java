/*
 *      KoresProxy - Proxy Pattern written on top of Kores! <https://github.com/JonathanxD/KoresProxy>
 *
 *         The MIT License (MIT)
 *
 *      Copyright (c) 2018 TheRealBuggy/JonathanxD (https://github.com/JonathanxD/ & https://github.com/TheRealBuggy/) <jonathan.scripter@programmer.net>
 *      Copyright (c) contributors
 *
 *
 *      Permission is hereby granted, free of charge, to any person obtaining a copy
 *      of this software and associated documentation files (the "Software"), to deal
 *      in the Software without restriction, including without limitation the rights
 *      to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *      copies of the Software, and to permit persons to whom the Software is
 *      furnished to do so, subject to the following conditions:
 *
 *      The above copyright notice and this permission notice shall be included in
 *      all copies or substantial portions of the Software.
 *
 *      THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *      IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *      FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *      AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *      LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *      OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *      THE SOFTWARE.
 */
package com.github.jonathanxd.koresproxy;

public class Debug {
    public static final String SAVE_PROXIES_KEY = "koresproxy.saveproxies";
    public static final String SAVE_DIRECTORY_KEY = "koresproxy.savedir";
    public static final String IGNORE_JAVA_MODULE_RULES_KEY = "koresproxy.ignore_module_rules";

    private static final boolean SAVE_PROXIES;
    private static final String SAVE_DIRECTORY;
    private static final boolean IGNORE_JAVA_MODULE_RULES;

    static {
        SAVE_PROXIES = Boolean.parseBoolean(System.getProperties().getProperty(SAVE_PROXIES_KEY, "false"));
        SAVE_DIRECTORY = System.getProperties().getProperty(SAVE_DIRECTORY_KEY, "gen/koresproxy/");
        IGNORE_JAVA_MODULE_RULES = Boolean.parseBoolean(
                System.getProperties().getProperty(IGNORE_JAVA_MODULE_RULES_KEY, "false"));
    }

    public static boolean isSaveProxies() {
        return Debug.SAVE_PROXIES;
    }

    public static boolean isIgnoreJavaModuleRules() {
        return Debug.IGNORE_JAVA_MODULE_RULES;
    }

    public static String getSaveDirectory() {
        return !SAVE_DIRECTORY.endsWith("/") ? SAVE_DIRECTORY + "/" : SAVE_DIRECTORY;
    }
}
