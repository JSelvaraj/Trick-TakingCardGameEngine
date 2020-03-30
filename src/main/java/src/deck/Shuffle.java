package src.deck;

import org.apache.commons.math3.random.MersenneTwister;

import java.util.List;


/**
 * Used to perform a random shuffle on a deck of cards, based on a provided seed.
 */
public class Shuffle {
    private MersenneTwister generator;

    public Shuffle(int seed) {
        this.generator = new MersenneTwister(seed);
    }

    /**
     * Seed the random number generator. If the generator hasn't been initialised, then it will be using the seed.
     * Otherwise the generator is reseeded.
     *
     * @param seed The seed for the generator.
     */
    public void seedGenerator(int seed) {
        generator.setSeed(seed);
    }

    /**
     * Shuffle a list using the Fisher Yates algorithm.
     *
     * @param list list to shuffle.
     * @param <T>  Type that implements this List interface, so that get and set can be used.
     * @param <E>  Type of the elements of T.
     */
    public <E, T extends List<E>> void shuffle(T list) {
        int j;
        for (int i = list.size() - 1; i > 0; i--) {
            j = Math.floorMod(generator.nextInt(), i + 1);
            //Swaps list[i] and list[j]
            E temp = list.get(i);
            list.set(i, list.get(j));
            list.set(j, temp);
        }
    }
}


