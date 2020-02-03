/*
 * Copyright (c) 2018, 2019, Gluon Software
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

import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.util.Callback;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;

public class SponsorBadge extends Badge {

    private static final SimpleDateFormat DATE_TIME_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public SponsorBadge() {
        
    }

    // The following fields and methods has to be duplicated because 
    // CloudlinkClient library doesn't support models with inheritance
    public SponsorBadge(String qr) {
        if (qr != null && ! qr.isEmpty() && qr.split("::").length == 5) {
            String[] split = qr.split("::");
            setBadgeId(split[0]);
            setLastName(split[1]);
            setFirstName(split[2]);
            setCompany(split[3]);
            setEmail(split[4]);
        }
    }

    // sponsor
    private final ObjectProperty<Sponsor> sponsor = new SimpleObjectProperty<>(this, "sponsor");
    public final ObjectProperty<Sponsor> sponsorProperty() {
       return sponsor;
    }
    public final Sponsor getSponsor() {
       return sponsor.get();
    }
    public final void setSponsor(Sponsor value) {
        sponsor.set(value);
    }

    // dateTime
    private final LongProperty dateTime = new SimpleLongProperty(this, "dateTime");
    public final LongProperty dateTimeProperty() {
       return dateTime;
    }
    public final long getDateTime() {
       return dateTime.get();
    }
    public final void setDateTime(long value) {
        dateTime.set(value);
    }
    
    // syncProperty
    private final BooleanProperty sync = new SimpleBooleanProperty(this, "sync", false);
    public final BooleanProperty syncProperty() {
       return sync;
    }
    public final boolean isSync() {
       return sync.get();
    }
    public final void setSync(boolean value) {
        sync.set(value);
    }

    @Override
    public boolean contains(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return false;
        }
        final String lowerKeyword = keyword.toLowerCase(Locale.ROOT);

        return containsKeyword(getFirstName(), lowerKeyword) || 
                containsKeyword(getLastName(), lowerKeyword) ||
                containsKeyword(getCompany(), lowerKeyword)  ||
                containsKeyword(getEmail(), lowerKeyword)    ||
                containsKeyword(getDetails(), lowerKeyword)  ||
                containsKeyword(getSponsor(), lowerKeyword);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SponsorBadge that = (SponsorBadge) o;
        return Objects.equals(getBadgeId(), that.getBadgeId()) &&
                Objects.equals(getSponsor(), that.getSponsor()) &&
                Objects.equals(getDateTime(), that.getDateTime()) &&
                Objects.equals(isSync(), that.isSync());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getBadgeId(), getSponsor(), getDateTime());
    }

    @Override
    public String toCSV() {
        StringBuilder csv = new StringBuilder(super.toCSV());
        csv.append(",").append(safeStr(getSponsor().getName()));
        if (getDateTime() != 0L) {
            csv.append(",").append(safeStr(DATE_TIME_FORMATTER.format(new Timestamp(getDateTime()))));
        }
        return csv.toString();
    }

    public static Callback<SponsorBadge, Observable[]> extractor() {
        return param -> new Observable[] {
                param.syncProperty()
        };
    }
}
