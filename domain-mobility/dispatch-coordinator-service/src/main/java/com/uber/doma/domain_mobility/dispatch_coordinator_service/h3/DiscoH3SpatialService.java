package com.uber.doma.domain_mobility.dispatch_coordinator_service.h3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DiscoH3SpatialService {
    private static final Logger log = LoggerFactory.getLogger(DiscoH3SpatialService.class);

    public List<String> getCandidateRings(String centerHex, int rings) {
        List<String> hexes = new ArrayList<>();
        hexes.add(centerHex);
        for (int i = 1; i <= rings; i++) {
            hexes.add(centerHex + "_k" + i);
        }
        log.info("[DiscoH3] Expanded search for {} across {} hexagonal rings", centerHex, rings);
        return hexes;
    }
}
