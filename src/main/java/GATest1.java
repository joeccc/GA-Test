import io.jenetics.BitChromosome;
import io.jenetics.BitGene;
import io.jenetics.Genotype;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.util.Factory;
import lombok.Data;

import java.util.*;

public class GATest1 {

    private final ArrayList<Pizza> pizzas = new ArrayList<>();
    private final ArrayList<Team> teams = new ArrayList<>();

    public static void main(String... args) {
        new GATest1().run();
    }

    @Data
    static class Pizza {
        Set<String> ingredients = new HashSet<>();
    }

    @Data
    static class Team {
        int size;
    }

    void run() {

        addPizza("onion", "pepper", "olive");
        addPizza("mushroom", "tomato", "basil");
        addPizza("chicken", "mushroom", "pepper");
        addPizza("tomato", "mushroom", "basil");
        addPizza("chicken", "basil");

        addTeam(2);
        addTeam(3);
        addTeam(3);
        addTeam(4);

        Factory<Genotype<BitGene>> gtf = Genotype.of(BitChromosome.of(10, 0.005));

        Engine<BitGene, Long> engine = Engine.builder(this::eval, gtf).build();

        System.out.println(engine.stream().findAny().get().bestPhenotype().genotype().chromosome());

        Genotype<BitGene> result = engine.stream()
                .limit(1000)
                .collect(EvolutionResult.toBestGenotype());

        System.out.println(result.chromosome());
    }

    private void addPizza(String... ingredients) {
        Pizza pizza = new Pizza();
        for (String ingredient : ingredients) {
            pizza.getIngredients().add(ingredient);
        }
        pizzas.add(pizza);
    }

    private void addTeam(int size) {
        Team team = new Team();
        team.setSize(size);
        teams.add(team);
    }

    // define however you want to evaluate the fitness of the chromosome
    private Long eval(Genotype<BitGene> gt) {
        BitChromosome bitChromosome = gt.chromosome().as(BitChromosome.class);
        Map<Integer, Set<Integer>> delivery = new HashMap<>();
        for (int i = 0; i < 5; i++) {
            int team = (bitChromosome.get(i * 2).booleanValue() ? 2 : 0) + (bitChromosome.get(i * 2 + 1).booleanValue() ? 1 : 0);
            delivery.computeIfAbsent(team, __ -> new HashSet<>()).add(i);
        }

        long score = 0;

        for (int i = 0; i < 4; i++) {
            Set<Integer> pizzasForTeam = delivery.getOrDefault(i, new HashSet<>());
            if (pizzasForTeam.size() == teams.get(i).getSize()) {
                Set<String> allIngredients = new HashSet<>();
                for (Integer n : pizzasForTeam) {
                    allIngredients.addAll(pizzas.get(n).getIngredients());
                }
                score += (long) allIngredients.size() * allIngredients.size();
            }
        }

        return score;
    }
}
