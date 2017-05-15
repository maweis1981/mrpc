package com.kongzhong.mrpc.registry;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.kongzhong.mrpc.cluster.Connections;
import com.kongzhong.mrpc.utils.JSONUtils;
import com.kongzhong.mrpc.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 默认服务发现
 *
 * @author biezhi
 *         2017/4/27
 */
@Slf4j
public class DefaultDiscovery implements ServiceDiscovery {

    public static final String DEFAULT_SWAP_NAME = "mrpc_registry_swap.lock";

    private File file = new File(DEFAULT_SWAP_NAME);

    public DefaultDiscovery() {

    }

    @Override
    public void discover() {
        try {
            String content = Files.toString(file, Charsets.UTF_8);
            if (StringUtils.isNotEmpty(content)) {
                List<Map<String, String>> array = JSONUtils.parseObject(content, List.class);
                Map<String, Set<String>> mappings = Maps.newHashMap();
                for (int i = 0, len = array.size(); i < len; i++) {
                    Map<String, String> object = array.get(i);
                    String serviceName = object.get("service");
                    String address = object.get("addr");

                    if (!mappings.containsKey(address)) {
                        mappings.put(address, Sets.newHashSet(serviceName));
                    } else {
                        mappings.get(address).add(serviceName);
                    }
                }
                Connections.me().updateNodes(mappings);
            }
        } catch (Exception e) {
            log.error("discover fail", e);
        }
    }

    @Override
    public void stop() {
    }

    private String read() {
        try {
            BufferedReader bf = new BufferedReader(new FileReader(DEFAULT_SWAP_NAME));
            return bf.readLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String read(InputStream input) throws IOException {
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input, "utf-8"))) {
            return buffer.lines().collect(Collectors.joining("\n"));
        }
    }

}

