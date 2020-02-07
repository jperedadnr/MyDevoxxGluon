/**
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
package com.devoxx;

import com.airhacks.afterburner.injection.Injector;
import com.devoxx.model.Badge;
import com.devoxx.model.BadgeType;
import com.devoxx.model.Conference;
import com.devoxx.model.Sponsor;
import com.devoxx.model.SponsorBadge;
import com.devoxx.service.DevoxxService;
import com.devoxx.service.Service;
import com.devoxx.util.*;
import com.devoxx.views.SessionsPresenter;
import com.devoxx.views.helper.ConnectivityUtils;
import com.devoxx.views.helper.SessionVisuals;
import com.devoxx.views.layer.ConferenceLoadingLayer;
import com.gluonhq.attach.connectivity.ConnectivityService;
import com.gluonhq.attach.device.DeviceService;
import com.gluonhq.attach.display.DisplayService;
import com.gluonhq.attach.settings.SettingsService;
import com.gluonhq.attach.share.ShareService;
import com.gluonhq.attach.storage.StorageService;
import com.gluonhq.attach.util.Platform;
import com.gluonhq.charm.glisten.afterburner.AppView;
import com.gluonhq.charm.glisten.afterburner.GluonInstanceProvider;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.gluonhq.cloudlink.client.usage.UsageClient;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.devoxx.DevoxxView.SEARCH;

public class DevoxxApplication extends MobileApplication {

    private static final Logger LOG = Logger.getLogger(DevoxxApplication.class.getName());
    public static final String POPUP_FILTER_SESSIONS_MENU = "FilterSessionsMenu";

    private static final GluonInstanceProvider instanceSupplier = new GluonInstanceProvider() {{
        bindProvider(Service.class, DevoxxService::new);
        bindProvider(DevoxxSearch.class, DevoxxSearch::new);
        bindProvider(DevoxxNotifications.class, DevoxxNotifications::new);
        bindProvider(SessionVisuals.class, SessionVisuals::new);

        Injector.setInstanceSupplier(this);
    }};

    private final Button navMenuButton   = MaterialDesignIcon.MENU.button(e -> getDrawer().open());
    private final Button navBackButton   = MaterialDesignIcon.ARROW_BACK.button(e -> switchToPreviousView());
    private final Button navHomeButton   = MaterialDesignIcon.HOME.button(e -> goHome());
    private final Button navSearchButton = MaterialDesignIcon.SEARCH.button(e -> SEARCH.switchView());

    private Service service;

    private boolean signUp = false;

    static {
        // Config logging
        DevoxxLogging.config();

        LOG.log(Level.INFO, "JAVA HOME " + System.getProperty("java.home"));
        LOG.log(Level.INFO, "USER HOME " + System.getProperty("user.home"));
        try {
            File root = StorageService.create().flatMap(StorageService::getPrivateStorage)
                    .orElseThrow(() -> new IOException("Error: Storage is required"));
            Path securityPath = Path.of(root.getAbsolutePath(), "lib", "security");
            LOG.log(Level.INFO, "securityPath = " + securityPath);
            if (!Files.exists(securityPath)) {
                if (!Files.exists(Path.of(root.getAbsolutePath()))) {
                    LOG.log(Level.INFO, "path0 = " + securityPath);
                    Files.createDirectories(Path.of(root.getAbsolutePath()));
                }
                if (!Files.exists(Path.of(root.getAbsolutePath(), "lib"))) {
                    LOG.log(Level.INFO, "path1 = " + securityPath);
                    Files.createDirectories(Path.of(root.getAbsolutePath(), "lib"));
                }
                if (!Files.exists(Path.of(root.getAbsolutePath(), "lib", "security"))) {
                    LOG.log(Level.INFO, "path2 = " + securityPath);
                    Files.createDirectories(Path.of(root.getAbsolutePath(), "lib", "security"));
                }
                LOG.log(Level.INFO, "path3 = " + securityPath);

                copyFileFromResources("/security/blacklisted.certs", securityPath.resolve("blacklisted.certs").toString());
                copyFileFromResources("/security/cacerts.remove", securityPath.resolve("cacerts").toString());
                copyFileFromResources("/security/default.policy", securityPath.resolve("default.policy").toString());
                copyFileFromResources("/security/public_suffix_list.dat", securityPath.resolve("public_suffix_list.dat").toString());
            }
            System.setProperty("java.home", root.getAbsolutePath());
            System.setProperty("javax.net.ssl.trustStore", securityPath.resolve("cacerts").toString());
            System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Storage Service Error", e);
        }
    }

    @Override
    public void init() {

        new UsageClient().enable();

        // start service data preloading as soon as possible
        service = Injector.instantiateModelOrService(Service.class);

        Injector.instantiateModelOrService(DevoxxNotifications.class);

        for (AppView view : DevoxxView.REGISTRY.getViews()) {
            view.registerView(this);
        }

        SettingsService.create().ifPresent(settings -> {
            String sign = settings.retrieve(DevoxxSettings.SIGN_UP);
            if (!Strings.isNullOrEmpty(sign)) {
                signUp = Boolean.parseBoolean(sign);
            }
        });
    }

    @Override
    public void postInit(Scene scene) {

        // Check if conference is set and switch to Sessions view
        final Conference conference = service.createConferenceFromLocalStorage();
        if (conference.getId() != null && conference.getName() != null && conference.getEventType() != null) {
            ConferenceLoadingLayer.show(service, conference);
        }

        String deviceFactorSuffix = DeviceService.create()
                .map(s -> {
                    if (Platform.isAndroid() && s.getModel() != null) {
                        for (String device : DevoxxSettings.DEVICES_WITH_SANS_CSS) {
                            if (s.getModel().toLowerCase(Locale.ROOT).contains(device)) {
                                return "_sans";
                            }
                        }
                    }
                    return "";
                })
                .orElse("");

        String formFactorSuffix = DisplayService.create()
                .map(s -> s.isTablet() ? "_tablet" : s.hasNotch() ? "_notch" : "")
                .orElse("");

        String stylesheetName = String.format("devoxx_%s%s%s.css",
                Platform.getCurrent().name().toLowerCase(Locale.ROOT),
                deviceFactorSuffix,
                formFactorSuffix);
        scene.getStylesheets().add(DevoxxApplication.class.getResource(stylesheetName).toExternalForm());

        String voxxedStylesheet = DevoxxApplication.class.getResource("voxxed.css").toExternalForm();
        addOrRemoveVoxxedStylesheet(scene, service.getConference(), voxxedStylesheet);
        service.conferenceProperty().addListener((observable, oldValue, newValue) -> {
            addOrRemoveVoxxedStylesheet(scene, newValue, voxxedStylesheet);
        });
        
        if (Platform.isDesktop()) {
            Window window = scene.getWindow();
            ((Stage) window).getIcons().add(new Image(DevoxxApplication.class.getResourceAsStream("/icon.png")));
            window.setWidth(350);
            window.setHeight(700);
        }
        
        Injector.instantiateModelOrService(DevoxxDrawerPresenter.class);

        scene.getWindow().showingProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    initConnectivityServices();
                    scene.getWindow().showingProperty().removeListener(this);
                }
            }
        });
        
        if (signUp) {
            SettingsService.create().ifPresent(settings -> settings.remove(DevoxxSettings.SIGN_UP));
            DevoxxView.SESSIONS.switchView().ifPresent(s -> ((SessionsPresenter) s).selectFavorite());
        }
    }

    private void addOrRemoveVoxxedStylesheet(Scene scene, Conference conference, String voxxedStylesheet) {
        if (conference != null && conference.getEventType() == Conference.Type.VOXXED) {
            if(!scene.getStylesheets().contains(voxxedStylesheet)) {
                scene.getStylesheets().add(voxxedStylesheet);
            }
        } else {
            scene.getStylesheets().remove(voxxedStylesheet);
        }
    }

    private void initConnectivityServices() {
        ConnectivityService.create().ifPresent(connectivityService -> {
            connectivityService.connectedProperty().addListener((observable, oldValue, newValue) -> {
                ConnectivityUtils.showConnectivityIndication(newValue);
            });

            ConnectivityUtils.showConnectivityIndication(connectivityService.isConnected());
        });

    }


    public Button getNavMenuButton() {
        return navMenuButton;
    }

    public Button getNavBackButton() {
        return navBackButton;
    }

    public Button getNavHomeButton() {
        return navHomeButton;
    }
    
    public Button getSearchButton() {
        return navSearchButton;
    }
    
    public Button getShareButton(BadgeType badgeType, Sponsor sponsor) {
        return MaterialDesignIcon.SHARE.button(e -> {
            ShareService.create().ifPresent(s -> {
                File root = StorageService.create().flatMap(storage -> storage.getPublicStorage("Documents")).orElse(null);
                if (root != null) {
                    if (!root.exists()) {
                        root.mkdirs();
                    }
                    File file = new File(root, "Devoxx" + DevoxxCountry.getConfShortName(service.getConference().getCountry()) + "-badges.csv");
                    if (file.exists()) {
                        file.delete();
                    }
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                        if (BadgeType.ATTENDEE == badgeType) {
                            writer.write("ID,First Name,Last Name,Company,Email,Details");
                            writer.newLine();
                            for (Badge badge : service.retrieveBadges()) {
                                writer.write(badge.toCSV());
                                writer.newLine();
                            }
                        } else if (BadgeType.SPONSOR == badgeType) {
                            writer.write("ID,First Name,Last Name,Company,Email,Details,Slug,Timestamp");
                            writer.newLine();
                            for (SponsorBadge badge : service.retrieveSponsorBadges(sponsor)) {
                                writer.write(badge.toCSV());
                                writer.newLine();
                            }
                        } else {
                            LOG.log(Level.WARNING, "Error invalid badgeType: " + badgeType);
                        }
                    } catch (IOException ex) {
                        LOG.log(Level.WARNING, "Error writing csv file ", ex);
                    }
                    s.share(DevoxxBundle.getString("OTN.BADGES.SHARE.SUBJECT", service.getConference().getName()),
//                            DevoxxBundle.getString("OTN.BADGES.SHARE.MESSAGE", service.getConference().getName(), DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).format(LocalDate.now())),
                            DevoxxBundle.getString("OTN.BADGES.SHARE.MESSAGE", service.getConference().getName(), LocalDate.now().toString()),
                            "text/plain", file);
                } else {
                    LOG.log(Level.WARNING, "Error accessing local storage");
                }
            });
        }); 
    }


    public static boolean copyFileFromResources(String pathIni, String pathEnd)  {
        try (InputStream myInput = DevoxxApplication.class.getResourceAsStream(pathIni)) {
            if (myInput == null) {
                LOG.log(Level.WARNING, "Error file " + pathIni + " not found");
                return false;
            }
            try (OutputStream myOutput = new FileOutputStream(pathEnd)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = myInput.read(buffer)) > 0) {
                    myOutput.write(buffer, 0, length);
                }
                myOutput.flush();
                LOG.log(Level.INFO, "File copied to " + pathEnd);
                return true;
            } catch (IOException ex) {
                LOG.log(Level.WARNING, "Error copying file", ex);
            }
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "Error copying file", ex);
        }
        return false;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
