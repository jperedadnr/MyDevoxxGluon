/*
 * Copyright (c) 2017, 2019, Gluon Software
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
import com.devoxx.model.SponsorBadge;
import com.devoxx.service.Service;
import com.devoxx.util.DevoxxBundle;
import com.devoxx.util.DevoxxSettings;
import com.devoxx.views.helper.Placeholder;
import com.gluonhq.charm.glisten.afterburner.GluonPresenter;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.control.Dialog;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import javax.inject.Inject;
import java.util.Objects;

public class BadgePresenter extends GluonPresenter<DevoxxApplication> {

    @FXML
    private View badgeView;
    
    @Inject
    private Service service;
    
    @FXML
    private TextField firstName;

    @FXML
    private TextField lastName;

    @FXML
    private TextField company;

    @FXML
    private TextField email;
    
    @FXML
    private TextArea details;
    
    private Badge badge;
    private boolean textChanged;
    private boolean scanned;
    private BadgeType badgeType;
    private ChangeListener<String> detailsChangeListener = (observable, oldValue, newValue) -> {
        if (newValue != null && !newValue.isEmpty()) {
            textChanged = true;
            if (badge instanceof SponsorBadge) {
                ((SponsorBadge) badge).setSync(false);
            }
        }
    };

    public void initialize() {
        badgeView.setOnShowing(event -> {
            AppBar appBar = getApp().getAppBar();
            appBar.setNavIcon(getApp().getNavBackButton());
            appBar.setTitleText(DevoxxView.BADGE.getTitle());
            appBar.getActionItems().add(MaterialDesignIcon.DELETE.button(e -> {
                final Dialog<Button> dialog = createDialog();
                dialog.showAndWait();
            }));
        });
        
        badgeView.setOnHiding(event -> {
            if (badge != null && (scanned || textChanged)) {
                saveBadge();
            }
            textChanged = false;
            details.textProperty().removeListener(detailsChangeListener);
        });
        
        // Fix for keyboard not showing on Android.
        // As TextArea is the only focusable control, 
        // it receives focus by default.
        // Set the focus on the View, such that TextArea receives
        // focus when tapped.
        badgeView.setOnShown(event -> badgeView.requestFocus());
    }

    private Dialog<Button> createDialog() {
        Dialog<Button> dialog = new Dialog<>();
        Placeholder deleteDialogContent = new Placeholder(DevoxxBundle.getString("OTN.BADGE.DIALOG.REMOVE.TITLE"), 
                DevoxxBundle.getString("OTN.BADGE.DIALOG.REMOVE.CONTENT"), MaterialDesignIcon.HELP);

        deleteDialogContent.setPrefWidth(MobileApplication.getInstance().getView().getScene().getWidth() - 40);

        dialog.setContent(deleteDialogContent);
        Button yesButton = new Button(DevoxxBundle.getString("OTN.LOGOUT_DIALOG.YES"));
        Button noButton = new Button(DevoxxBundle.getString("OTN.LOGOUT_DIALOG.NO"));
        yesButton.setOnAction(ev -> {
            removeBadge();
            badge = null;
            dialog.hide();
            getApp().switchToPreviousView();
        });
        noButton.setOnAction(ev -> dialog.hide());
        dialog.getButtons().addAll(noButton, yesButton);
        return dialog;
    }

    private void removeBadge() {
        if (badgeType == BadgeType.ATTENDEE) {
            service.retrieveBadges().remove(badge);
        } else {
            service.retrieveSponsorBadges(((SponsorBadge)badge).getSponsor()).remove(badge);
        }
    }

    public void setBadge(Badge badge, BadgeType badgeType, boolean scanned) {
        Objects.requireNonNull(badge);
        Objects.requireNonNull(badge.getBadgeId());

        this.badge = badge;
        this.badgeType = badgeType;
        this.scanned = scanned;
        
        if (badgeType == BadgeType.ATTENDEE) {
            if (service.isAuthenticated() || !DevoxxSettings.USE_REMOTE_NOTES) {
                final ObservableList<Badge> badges = service.retrieveBadges();
                if (badges.contains(badge)) {
                    this.badge = badge;
                }
            }
        } else {
            final SponsorBadge sponsorBadge = (SponsorBadge) badge;
            final ObservableList<SponsorBadge> badges = service.retrieveSponsorBadges(sponsorBadge.getSponsor());
            if (badges.contains(badge)) {
                this.badge = badge;
            }
        }
        if (this.badge != null) {
            firstName.setText(badge.getFirstName());
            lastName.setText(badge.getLastName());
            company.setText(badge.getCompany());
            email.setText(badge.getEmail());
            details.setText(badge.getDetails());
        }
        details.textProperty().addListener(detailsChangeListener);
    }
    
    private void saveBadge() {

        badge.setFirstName(firstName.getText());
        badge.setLastName(lastName.getText());
        badge.setCompany(company.getText());
        badge.setEmail(email.getText());
        badge.setDetails(details.getText());
        
        if (badge instanceof SponsorBadge) {
            // every scanned sponsor badge must be posted with the remote function
            service.saveSponsorBadge((SponsorBadge) badge);
        }
    }

}
