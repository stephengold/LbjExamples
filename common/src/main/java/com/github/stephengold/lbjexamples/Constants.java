/*
 Copyright (c) 2022, Stephen Gold and Yanis Boudiaf
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 1. Redistributions of source code must retain the above copyright notice, this
    list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. Neither the name of the copyright holder nor the names of its
    contributors may be used to endorse or promote products derived from
    this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.stephengold.lbjexamples;

import org.joml.Vector4f;
import org.joml.Vector4fc;

/**
 * Constants used in the LbjExamples project.
 *
 * @author Stephen Gold sgold@sonic.net
 */
final public class Constants {
    // *************************************************************************
    // constants

    /**
     * The color black (0, 0, 0).
     */
    final public static Vector4fc BLACK = new Vector4f(0f, 0f, 0f, 1f);
    /**
     * The color blue (0, 0, 1).
     */
    final public static Vector4fc BLUE = new Vector4f(0f, 0f, 1f, 1f);
    /**
     * The opaque color "dark gray" (0.1, 0.1, 0.1).
     */
    final public static Vector4fc DARK_GRAY
            = new Vector4f(0.1f, 0.1f, 0.1f, 1f);
    /**
     * The color green (0, 1, 0).
     */
    final public static Vector4fc GREEN = new Vector4f(0f, 1f, 0f, 1f);
    /**
     * The color magenta (1, 0, 1).
     */
    final public static Vector4fc MAGENTA = new Vector4f(1f, 0f, 1f, 1f);
    /**
     * The color red (1, 0, 0).
     */
    final public static Vector4fc RED = new Vector4f(1f, 0f, 0f, 1f);
    /**
     * The opaque color "sky blue" (0.35, 0.48, 0.66).
     */
    final public static Vector4fc SKY_BLUE
            = new Vector4f(0.35f, 0.48f, 0.66f, 1f);
    /**
     * The color white (1, 1, 1).
     */
    final public static Vector4fc WHITE = new Vector4f(1f, 1f, 1f, 1f);
    // *************************************************************************
    // constructors

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private Constants() {
        // do nothing
    }
}
