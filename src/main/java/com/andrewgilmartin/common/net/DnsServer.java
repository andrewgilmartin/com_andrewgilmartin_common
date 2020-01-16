package com.andrewgilmartin.common.net;

import com.andrewgilmartin.common.util.ToolBase;
import com.andrewgilmartin.common.util.logger.CommonLogger;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.CNAMERecord;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Flags;
import org.xbill.DNS.Header;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Rcode;
import org.xbill.DNS.Record;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.Section;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

/**
 * This is a simple, single threaded, DNS server. The DNS records are defined on
 * the command line. For example, {@code
 *
 * net.DnsServer \
 *    --ttl 10 \
 *    --a newapp. 1.2.3.4 \
 *    --cname newapp. oldapp. \
 *    --srv newapp. my.domain.com. 8080
 *
 * It is intended for developer use, primarily.
 *
 * host -t CNAME newapp 127.0.0.1
 *
 * Based on
 * https://www.programcreek.com/java-api-examples/?code=shred/acme4j/acme4j-master/acme4j-it/src/main/java/org/shredzone/acme4j/it/server/DnsServer.java
 */
public class DnsServer extends ToolBase {

    private static final CommonLogger logger = CommonLogger.getLogger(DnsServer.class);

    private static final int UDP_SIZE = 512;
    private static final int DEFAULT_DNS_PORT = 53;
    private static final int DEFAULT_TTL = 300;
    private static final int DEFAULT_PRIORITY = 1;
    private static final int DEFAULT_WEIGHT = 1;
    private static final Integer[] RECORD_TYPES = new Integer[]{
        Type.A,
        Type.A6,
        Type.AAAA,
        Type.AFSDB,
        Type.ANY,
        Type.APL,
        Type.ATMA,
        Type.AXFR,
        Type.CAA,
        Type.CERT,
        Type.CNAME,
        Type.DHCID,
        Type.DLV,
        Type.DNAME,
        Type.DNSKEY,
        Type.DS,
        Type.EID,
        Type.GPOS,
        Type.HINFO,
        Type.IPSECKEY,
        Type.ISDN,
        Type.IXFR,
        Type.KEY,
        Type.KX,
        Type.LOC,
        Type.MAILA,
        Type.MAILB,
        Type.MB,
        Type.MD,
        Type.MF,
        Type.MG,
        Type.MINFO,
        Type.MR,
        Type.MX,
        Type.NAPTR,
        Type.NIMLOC,
        Type.NS,
        Type.NSAP,
        Type.NSAP_PTR,
        Type.NSEC,
        Type.NSEC3,
        Type.NSEC3PARAM,
        Type.NULL,
        Type.NXT,
        Type.OPENPGPKEY,
        Type.OPT,
        Type.PTR,
        Type.PX,
        Type.RP,
        Type.RRSIG,
        Type.RT,
        Type.SIG,
        Type.SMIMEA,
        Type.SOA,
        Type.SPF,
        Type.SRV,
        Type.SSHFP,
        Type.TKEY,
        Type.TLSA,
        Type.TSIG,
        Type.TXT,
        Type.URI,
        Type.WKS,
        Type.X25
    };

    private int port = DEFAULT_DNS_PORT;
    private int ttl = DEFAULT_TTL;
    private int weight = DEFAULT_WEIGHT;
    private int priority = DEFAULT_PRIORITY;
    private final Map<Integer, Multimap<Name, Record>> typeToRecords = new HashMap<>();

    public DnsServer() {
        for (int i = 0; i < RECORD_TYPES.length; i++) {
            typeToRecords.put(RECORD_TYPES[i], MultimapBuilder.hashKeys().hashSetValues().build());
        }
    }

    public void setPort(@com.andrewgilmartin.common.annotations.Name("number") int port) {
        this.port = port;
    }

    public void setTtl(@com.andrewgilmartin.common.annotations.Name("seconds") int ttl) {
        this.ttl = ttl;
    }

    public void setWeight(@com.andrewgilmartin.common.annotations.Name("number") int weight) {
        this.weight = weight;
    }

    public void setPriority(@com.andrewgilmartin.common.annotations.Name("number") int priority) {
        this.priority = priority;
    }

    public void addSrv(
            @com.andrewgilmartin.common.annotations.Name("name") String name,
            @com.andrewgilmartin.common.annotations.Name("target") String target,
            @com.andrewgilmartin.common.annotations.Name("port") int port
    ) throws TextParseException {
        SRVRecord r = new SRVRecord(new Name(name), DClass.IN, ttl, priority, weight, port, new Name(target));
        typeToRecords.get(Type.SRV).put(r.getName(), r);
        typeToRecords.get(Type.ANY).put(r.getName(), r);
    }

    public void addA(
            @com.andrewgilmartin.common.annotations.Name("name") String name,
            @com.andrewgilmartin.common.annotations.Name("ip") String ip
    ) throws TextParseException, UnknownHostException {
        ARecord r = new ARecord(new Name(name), DClass.IN, ttl, InetAddress.getByName(ip));
        typeToRecords.get(Type.A).put(r.getName(), r);
        typeToRecords.get(Type.ANY).put(r.getName(), r);
    }

    public void addCname(
            @com.andrewgilmartin.common.annotations.Name("name") String name,
            @com.andrewgilmartin.common.annotations.Name("alias") String alias
    ) throws TextParseException, UnknownHostException {
        CNAMERecord r = new CNAMERecord(new Name(alias), DClass.IN, ttl, new Name(name));
        typeToRecords.get(Type.CNAME).put(r.getName(), r);
        typeToRecords.get(Type.ANY).put(r.getName(), r);
    }

    public <T extends Record> Collection<T> findRecords(int type, String name) throws TextParseException {
        Multimap<Name, Record> namedRecords = typeToRecords.get(type);
        if (namedRecords != null) {
            return (Collection<T>) namedRecords.get(new Name(name));
        }
        return null;
    }

    @Override
    public void run() {
        try (DatagramSocket sock = new DatagramSocket(port)) {
            for (;;) {
                process(sock);
            }
        } catch (IOException e) {
            logger.error(e, "failed to open UDP socket");
        }
    }

    private void process(DatagramSocket sock) {
        try {
            byte[] in = new byte[UDP_SIZE];

            DatagramPacket indp = new DatagramPacket(in, UDP_SIZE);
            indp.setLength(UDP_SIZE);
            sock.receive(indp);
            Message msg = new Message(in);
            Header header = msg.getHeader();
            Record question = msg.getQuestion();

            Message response = new Message(header.getID());
            response.getHeader().setFlag(Flags.QR);
            response.addRecord(question, Section.QUESTION);

            Name name = question.getName();
            boolean hasRecords = false;

            Multimap<Name, Record> namedRecords = typeToRecords.get(question.getType());
            if (namedRecords != null) {
                for (Record record : namedRecords.get(name)) {
                    response.addRecord(record, Section.ANSWER);
                    hasRecords = true;
                }
            }

            if (!hasRecords) {
                response.getHeader().setRcode(Rcode.NXDOMAIN);
            }

            // Send the response
            byte[] resp = response.toWire();
            DatagramPacket outdp = new DatagramPacket(resp, resp.length, indp.getAddress(), indp.getPort());
            sock.send(outdp);
        } catch (IOException e) {
            logger.error(e, "failed to process query");
        }
    }

    @Override
    public void execute() {
        run();
    }

    public static void main(String... args) throws Exception {
        ToolBase tool = new DnsServer();
        tool.run(args);
    }
}

// END
