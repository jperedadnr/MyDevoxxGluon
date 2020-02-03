/*
 * Copyright (c) 2019, Gluon Software
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation and/or other materials provided
 *    with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse
 *    or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.devoxx.model;

import com.devoxx.util.DevoxxBundle;

import java.util.Arrays;

public enum AudienceLevel {
    BEGINNER     ("OTN.SESSION.AUDIENCE_LEVEL.BEGINNER"),
    INTERMEDIATE ("OTN.SESSION.AUDIENCE_LEVEL.INTERMEDIATE"),
    ADVANCED     ("OTN.SESSION.AUDIENCE_LEVEL.ADVANCED"),
    EXPERT       ("OTN.SESSION.AUDIENCE_LEVEL.EXPERT");

    private String resourceName;

    AudienceLevel(String resourceName) {
        this.resourceName = resourceName;
    }

    /**
     * Checks if a string can be mapped to any of the values in {@link AudienceLevel}.
     * @param audienceLevel string to test
     * @return True if the string matches one of the values, else false.
     */
    public static boolean contains(String audienceLevel) {
        try {
            return Arrays.asList(AudienceLevel.values()).contains(AudienceLevel.valueOf(audienceLevel));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Returns the text to be displayed for an {@link AudienceLevel}.
     * @return Text to be displayed for an {@link AudienceLevel}.
     */
    public String getText() {
        return DevoxxBundle.getString(resourceName);
    }
}
