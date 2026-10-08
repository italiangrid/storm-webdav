// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.server.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import eu.emi.security.authn.x509.StoreUpdateListener.Severity;
import eu.emi.security.authn.x509.ValidationError;
import eu.emi.security.authn.x509.ValidationErrorCode;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import org.italiangrid.storm.webdav.error.StoRMIntializationError;
import org.italiangrid.voms.ac.VOMSValidationResult;
import org.italiangrid.voms.store.impl.LSCFile;
import org.junit.jupiter.api.Test;
import org.springframework.boot.diagnostics.FailureAnalysis;

class ServerUtilsTest {
  public static class TestLogAppender extends AppenderBase<ILoggingEvent> {
    private final StringBuilder logs = new StringBuilder();

    @Override
    protected void append(ILoggingEvent eventObject) {
      logs.append(eventObject.getFormattedMessage()).append("");
    }

    public String getLogs() {
      return logs.toString();
    }
  }

  @Test
  void stormFailureAnalyzerTests() {
    StormFailureAnalyzer analyzer = new StormFailureAnalyzer();
    StoRMIntializationError failure =
        new StoRMIntializationError("InitializationError", new Throwable());
    FailureAnalysis analysis = analyzer.analyze(failure);
    assertEquals(
        "Storm WebDAV could not start due to an error: InitializationError",
        analysis.getDescription());
    assertEquals("Check the WebDAV service configuration!", analysis.getAction());
    assertEquals(failure, analysis.getCause());
  }

  private static X509Certificate buildTestCertificate() throws CertificateException {
    String cert =
        """
        -----BEGIN CERTIFICATE-----
        MIIBuzCCAW2gAwIBAgIUSu6McBuSaAPf5rsLUE3amCy8QB4wBQYDK2VwMEUxCzAJ
        BgNVBAYTAkFVMRMwEQYDVQQIDApTb21lLVN0YXRlMSEwHwYDVQQKDBhJbnRlcm5l
        dCBXaWRnaXRzIFB0eSBMdGQwHhcNMjYxMDI3MDkyOTUxWhcNMjcxMDI3MDkyOTUx
        WjBFMQswCQYDVQQGEwJBVTETMBEGA1UECAwKU29tZS1TdGF0ZTEhMB8GA1UECgwY
        SW50ZXJuZXQgV2lkZ2l0cyBQdHkgTHRkMCowBQYDK2VwAyEA02sb+d3PrSvIjH40
        vsbO60BAAWi7nwqvXZxqvyuD9XqjbzBtMB0GA1UdDgQWBBQoFG8ys9dsjkMqX/Tp
        bbaTH+WCnTAfBgNVHSMEGDAWgBQoFG8ys9dsjkMqX/TpbbaTH+WCnTAPBgNVHRMB
        Af8EBTADAQH/MBoGA1UdEQQTMBGCCWxvY2FsaG9zdIcEfwAAATAFBgMrZXADQQCc
        +gqquShlwqULa3Oxr4ZmEJKzA8YoOJxovKrSJe2yoDckk/PvXffe8LmLs2liOtTy
        WLLynFRZj9VUncWOS4cI
        -----END CERTIFICATE-----""";
    ByteArrayInputStream in = new ByteArrayInputStream(cert.getBytes());
    CertificateFactory cf = CertificateFactory.getInstance("X.509");
    return (X509Certificate) cf.generateCertificate(in);
  }

  @Test
  void canlListenerTests() throws CertificateException {
    CANLListener l = new CANLListener();
    TestLogAppender logAppender = new TestLogAppender();
    logAppender.start();
    Logger logger = (Logger) CANLListener.LOG;
    logger.addAppender(logAppender);

    logger.setLevel(Level.DEBUG);

    l.loadingNotification(
        "file:/path", "ErrorType", Severity.ERROR, new Exception("Exception occurred (error)"));
    l.loadingNotification(
        "/path", "WarningType", Severity.WARNING, new Exception("Exception occurred (warning)"));
    l.loadingNotification(
        "/path",
        "NotificationType",
        Severity.NOTIFICATION,
        new Exception("This message should not be displayed"));

    X509Certificate certificate = buildTestCertificate();
    X509Certificate[] chain = new X509Certificate[1];
    chain[0] = certificate;
    ValidationError error = new ValidationError(chain, 0, ValidationErrorCode.certificateExpired);
    assertFalse(l.onValidationError(error));

    String logs = logAppender.getLogs();
    System.out.print(logs);
    assertTrue(logs.contains("Error for ErrorType /path: Exception occurred (error)."));
    assertTrue(logs.contains("Warning for WarningType /path: Exception occurred (warning)."));
    assertTrue(logs.contains("Loading NotificationType /path."));
    assertFalse(logs.contains("This message should not be displayed"));
    assertTrue(
        logs.contains(
            """
            Certificate validation error for chain: Certificate chain, 1 elements:
            -----Certificate 1-----
            X.509 v3 certificate, Subject: O=Internet Widgits Pty Ltd,ST=Some-State,C=AU, Issuer: O=Internet Widgits Pty Ltd,ST=Some-State,C=AU
            . Error: Certificate has expired on: {0}"""));
  }

  @Test
  void vomsListenerTests() {
    VOMSListener l = new VOMSListener();
    TestLogAppender logAppender = new TestLogAppender();
    logAppender.start();
    Logger logger = (Logger) VOMSListener.LOG;
    logger.addAppender(logAppender);

    logger.setLevel(Level.TRACE);

    l.notifyLSCLookupEvent("/path/dir");
    assertTrue(logAppender.getLogs().contains("Looking for LSC files in /path/dir"));

    LSCFile lscFile = new LSCFile();
    lscFile.setFilename("lscFileName.lsc");
    l.notifyLSCLoadEvent(lscFile, new File("/path/lscFileName.lsc"));
    assertTrue(
        logAppender
            .getLogs()
            .contains(
                "Loaded LSC LSCFile [filename=lscFileName.lsc, vo=null, hostname=null, certChainDescription=[]] from /path/lscFileName.lsc"));

    VOMSValidationResult result = new VOMSValidationResult(null, false);
    l.notifyValidationResult(result);
    assertTrue(
        logAppender
            .getLogs()
            .contains(
                "VOMS attributes validation failed: VOMSValidationResult [valid=false, validationErrors=[], attributes=null]"));
    result = new VOMSValidationResult(null, true);
    l.notifyValidationResult(result);
    assertTrue(
        logAppender
            .getLogs()
            .contains(
                "VOMS attributes validation success: VOMSValidationResult [valid=true, validationErrors=[], attributes=null]"));
  }
}
