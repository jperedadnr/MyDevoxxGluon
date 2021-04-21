/*
 * Copyright (c) 2016, 2019, Gluon Software
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
package com.devoxx.views;

import com.devoxx.DevoxxApplication;
import com.devoxx.DevoxxView;
import com.devoxx.model.Badge;
import com.devoxx.model.BadgeType;
import com.devoxx.model.Sponsor;
import com.devoxx.model.SponsorBadge;
import com.devoxx.service.Service;
import com.devoxx.util.DevoxxBundle;
import com.devoxx.util.DevoxxSettings;
import com.devoxx.views.cell.SponsorBadgeCell;
import com.devoxx.views.helper.Placeholder;
import com.devoxx.views.helper.Util;
import com.gluonhq.attach.util.Platform;
import com.gluonhq.attach.barcodescan.BarcodeScanService;
import com.gluonhq.attach.connectivity.ConnectivityService;
import com.gluonhq.attach.settings.SettingsService;
import com.gluonhq.charm.glisten.afterburner.GluonPresenter;
import com.gluonhq.charm.glisten.application.ViewStackPolicy;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.control.CharmListView;
import com.gluonhq.charm.glisten.control.FloatingActionButton;
import com.gluonhq.charm.glisten.control.Toast;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.gluonhq.connect.GluonObservableList;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;

import javax.inject.Inject;
import java.util.Optional;

public class SponsorBadgePresenter extends GluonPresenter<DevoxxApplication> {

    private static final String EMPTY_LIST_MESSAGE = DevoxxBundle.getString("OTN.BADGES.EMPTY_LIST_MESSAGE");

    @FXML
    private View sponsorView;

    @Inject
    private Service service;

    private Sponsor sponsor;
    private FloatingActionButton scan;
    private Button syncButton;
    private CharmListView<SponsorBadge, String> sponsorBadges = new CharmListView<>();

    public void initialize() {

        scan = new FloatingActionButton();
        scan.getStyleClass().add("badge-scanner");
        scan.showOn(sponsorView);
        
        syncButton = MaterialDesignIcon.SYNC.button(e -> syncSponsorBadges());

        sponsorBadges.setPlaceholder(new Placeholder(EMPTY_LIST_MESSAGE, DevoxxView.SPONSOR_BADGE.getMenuIcon()));
        sponsorBadges.setCellFactory(param -> new SponsorBadgeCell());
        sponsorView.setCenter(sponsorBadges);

        sponsorView.setOnShowing(event -> {
            AppBar appBar = getApp().getAppBar();
            appBar.setNavIcon(getApp().getNavMenuButton());
            appBar.setTitleText(DevoxxView.SPONSOR_BADGE.getTitle());
            appBar.getMenuItems().setAll(getBadgeChangeMenuItem("Logout"));

            SettingsService.create().ifPresent(service -> {
                final Sponsor sponsor = Sponsor.fromCSV(service.retrieve(DevoxxSettings.BADGE_SPONSOR));
                if (this.sponsor != null && this.sponsor.equals(sponsor)) {
                    appBar.setTitleText(DevoxxBundle.getString("OTN.SPONSOR.BADGES.FOR", sponsor.getName()));
                    // Fix for Github issue: 229
                    setSponsor(sponsor);
                }
            });
        });
    }

    public void setSponsor(Sponsor sponsor) {
        this.sponsor = sponsor;
        // TODO: Call the RF only if the sponsor has changed
        loadSponsorBadges(sponsor);
    }

    private void loadSponsorBadges(Sponsor sponsor) {

        final GluonObservableList<SponsorBadge> badgesList = service.retrieveSponsorBadges(sponsor);
        ObservableList<SponsorBadge> badges = FXCollections.observableArrayList(SponsorBadge.extractor());
        Bindings.bindContentBidirectional(badges, badgesList);
        badgesList.setOnSucceeded(e -> {
            final FilteredList<SponsorBadge> filteredBadges = new FilteredList<>(badges, badge -> {
                return badge != null && badge.getSponsor() != null && badge.getSponsor().equals(sponsor);
            });
            sponsorBadges.setItems(filteredBadges);
        });

        final Button shareButton = getApp().getShareButton(BadgeType.SPONSOR, sponsor);
        shareButton.disableProperty().bind(sponsorBadges.itemsProperty().emptyProperty());
        AppBar appBar = getApp().getAppBar();
        appBar.setTitleText(DevoxxBundle.getString("OTN.SPONSOR.BADGES.FOR", sponsor.getName()));
        appBar.getActionItems().setAll(syncButton, shareButton);

        scan.setOnAction(e -> {
            if (DevoxxSettings.BADGE_TESTS) {
                addBadge(sponsor, badges, Util.getDummyQR());
                return;
            }
            BarcodeScanService.create().ifPresent(s -> {
                final Optional<String> scanQr = s.scan(DevoxxBundle.getString("OTN.BADGES.SPONSOR.QR.TITLE", sponsor.getName()), null, null);
                scanQr.ifPresent(qr -> addBadge(sponsor, badges, qr));
            });
        });
    }

    private void addBadge(Sponsor sponsor, ObservableList<SponsorBadge> badges, String qr) {
        SponsorBadge badge = new SponsorBadge(qr);
        if (badge.getBadgeId() != null) {
            boolean exists = false;
            for (Badge b : badges) {
                if (b != null && b.getBadgeId() != null && b.getBadgeId().equals(badge.getBadgeId())) {
                    Toast toast = new Toast(DevoxxBundle.getString("OTN.BADGES.QR.EXISTS"));
                    toast.show();
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                badge.setSponsor(sponsor);
                badge.setDateTime(System.currentTimeMillis());
                badges.add(badge);
                // Keep SponsorBadgeView on view stack 
                DevoxxView.BADGE.switchView(ViewStackPolicy.USE).ifPresent(presenter -> ((BadgePresenter) presenter).setBadge(badge, BadgeType.SPONSOR, true));
            }
        } else {
            Toast toast = new Toast(DevoxxBundle.getString("OTN.BADGES.BAD.QR"));
            toast.show();
        }
    }

    private MenuItem getBadgeChangeMenuItem(String text) {
        final MenuItem scanAsDifferentUser = new MenuItem(text);
        scanAsDifferentUser.setOnAction(e -> {
            Util.removeKeysFromSettings(DevoxxSettings.BADGE_TYPE, DevoxxSettings.BADGE_SPONSOR);
            service.logoutSponsor();
            DevoxxView.BADGES.switchView();
        });
        return scanAsDifferentUser;
    }

    private void syncSponsorBadges() {
        if (Platform.isDesktop()) {
            sync();
        } else {
            ConnectivityService.create().ifPresent(connectivityService -> {
                if (connectivityService.isConnected()) {
                    sync();
                } else {
                    showSyncFailureMessage();
                }
            });
        }
    }

    private void showSyncFailureMessage() {
        final Toast toast = new Toast();
        toast.setMessage(DevoxxBundle.getString("OTN.VISUALS.NO_INTERNET"));
        toast.show();
    }

    private void sync() {
        final Toast toast = new Toast();
        toast.setMessage(DevoxxBundle.getString("OTN.SPONSOR.BADGES.SYNC"));
        toast.show();
        for (SponsorBadge sponsorBadge : service.retrieveSponsorBadges(sponsor)) {
            if (!sponsorBadge.isSync()) {
                service.saveSponsorBadge(sponsorBadge);
            }
        }
    }
}
