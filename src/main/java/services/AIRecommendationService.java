package services;

import models.Partner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AIRecommendationService {

    /**
     * Recommends the best collaborators for a given partner based on a weighted similarity score.
     * Scoring factors: 
     * - Complementarity (40%): Producers matched with Distributors/Processors, etc.
     * - Proximity (30%): Partners in the same city.
     * - Reliability/Scale (30%): Simulated ranking based on name/length for demo.
     */
    public List<Partner> getRecommendations(Partner target, List<Partner> allPartners) {
        if (target == null || allPartners == null) return new ArrayList<>();

        // Exclude the current partner
        List<Partner> candidates = allPartners.stream()
                .filter(p -> p.getId() != target.getId())
                .collect(Collectors.toList());

        List<ScoredPartner> scoredList = new ArrayList<>();

        for (Partner p : candidates) {
            double score = calculateScore(target, p);
            scoredList.add(new ScoredPartner(p, score));
        }

        // Sort by score descending
        Collections.sort(scoredList, Comparator.comparingDouble(ScoredPartner::getScore).reversed());

        // Return top 3
        return scoredList.stream()
                .limit(3)
                .map(ScoredPartner::getPartner)
                .collect(Collectors.toList());
    }

    private double calculateScore(Partner target, Partner candidate) {
        double score = 0;

        // 1. Complementarity Logic (Higher is better)
        String tType = target.getType();
        String cType = candidate.getType();

        if (tType.equals("Producteur")) {
            if (cType.equals("Distributeur")) score += 40;
            if (cType.equals("Transformateur")) score += 35;
        } else if (tType.equals("Distributeur")) {
            if (cType.equals("Producteur")) score += 40;
            if (cType.equals("Transformateur")) score += 30;
        } else if (tType.equals("Transformateur")) {
            if (cType.equals("Producteur")) score += 40;
            if (cType.equals("Distributeur")) score += 35;
        } else if (tType.equals("Investisseur")) {
            score += 40; // Investors are matched with everyone
        }

        // 2. Proximity Logic (Simple city matching)
        if (target.getAddress() != null && candidate.getAddress() != null) {
            String targetCity = extractCity(target.getAddress());
            String candidateCity = extractCity(candidate.getAddress());
            if (targetCity.equalsIgnoreCase(candidateCity)) {
                score += 30;
            }
        }

        // 3. Simulated Popularity (Random/Name based)
        score += (candidate.getName().length() % 30);

        return score;
    }

    private String extractCity(String address) {
        if (address.contains(",")) return address.split(",")[0].trim();
        return address.trim();
    }

    private static class ScoredPartner {
        private Partner partner;
        private double score;

        public ScoredPartner(Partner partner, double score) {
            this.partner = partner;
            this.score = score;
        }

        public Partner getPartner() { return partner; }
        public double getScore() { return score; }
    }
}
