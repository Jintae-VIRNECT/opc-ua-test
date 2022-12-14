/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jens Reimann - initial API and implementation
 *******************************************************************************/
package com.example.opcuademo.common;

public final class Constants {
    private Constants() {
    }

    // public static final String HOST = "apollo.muc.redhat.com";
    public static final String HOST = "opcuaserver.com";
    // public static final String HOST = "[fe80::2c82:aeff:fe0b:ac2%enp0s31f6]";
    // public static final String HOST = "[fe80::458b:2d95:d98a:7ffc%enp0s31f6]";
    // public static final String HOST = "[fe80::9289:e377:bacb:f608%enp0s31f6]";
    // public static final String HOST = System.getProperty("host",
    // "jreimann-up2-1.muc.redhat.com");
    public static final int PORT = Integer.getInteger("port", 48010);
}
