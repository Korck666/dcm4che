/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2012
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4che.conf.ldap.hl7;

import static org.junit.Assert.assertEquals;

import org.dcm4che.conf.api.ConfigurationNotFoundException;
import org.dcm4che.conf.ldap.LdapEnv;
import org.dcm4che.net.Connection;
import org.dcm4che.net.hl7.HL7Application;
import org.dcm4che.net.hl7.HL7Device;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class LdapHL7ConfigurationTest {

    private LdapHL7Configuration config;

    @Before
    public void setUp() throws Exception {
        LdapEnv env = new LdapEnv();
        env.setUrl("ldap://localhost:389");
        env.setUserDN("cn=admin,dc=nodomain");
        env.setPassword("admin");
//        env.setUrl("ldap://localhost:1389");
//        env.setUserDN("cn=Directory Manager");
//        env.setPassword("admin");
//        env.setUrl("ldap://localhost:10389");
//        env.setUserDN("uid=admin,ou=system");
//        env.setPassword("secret");
        config = new LdapHL7Configuration(env, "dc=nodomain");
    }

    @After
    public void tearDown() throws Exception {
        config.close();
    }

    @Test
    public void testPersist() throws Exception {
        try {
            config.removeDevice("Test-Device-1");
        }  catch (ConfigurationNotFoundException e) {}
        HL7Device device = createDevice("Test-Device-1", "TEST1^DCM4CHE");
        config.persist(device);
        HL7Application app = config.findHL7Application("TEST1^DCM4CHE");
        assertEquals(2575, app.getConnections().get(0).getPort());
        assertEquals("TEST2^DCM4CHE", app.getAcceptedSendingApplications()[0]);
        assertEquals(7, app.getAcceptedMessageTypes().length);
        config.removeDevice("Test-Device-1");
    }

    private static HL7Device createDevice(String name, String appName) throws Exception {
        HL7Device device = new HL7Device(name);
        Connection conn = createConn("host.dcm4che.org", 2575);
        device.addConnection(conn);
        HL7Application app = createHL7App(appName, conn);
        device.addHL7Application(app);
        return device ;
    }

    private static Connection createConn(String hostname, int port) {
        Connection conn = new Connection();
        conn.setHostname(hostname);
        conn.setPort(port);
        return conn;
    }

    private static HL7Application createHL7App(String name, Connection conn) {
        HL7Application app = new HL7Application(name);
        app.addConnection(conn);
        app.setAcceptedSendingApplications("TEST2^DCM4CHE");
        app.setAcceptedMessageTypes(
                "ADT^A02",
                "ADT^A03",
                "ADT^A06",
                "ADT^A07",
                "ADT^A08",
                "ADT^A40",
                "ORM^O01");
        return app;
    }
}