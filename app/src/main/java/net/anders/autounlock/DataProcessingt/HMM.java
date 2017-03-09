package net.anders.autounlock.DataProcessingt;

import java.util.ArrayList;
import java.util.List;

import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.Observation;
import be.ac.ulg.montefiore.run.jahmm.ObservationDiscrete;
import be.ac.ulg.montefiore.run.jahmm.OpdfDiscrete;
import be.ac.ulg.montefiore.run.jahmm.OpdfDiscreteFactory;
import be.ac.ulg.montefiore.run.jahmm.draw.GenericHmmDrawerDot;
import be.ac.ulg.montefiore.run.jahmm.learn.BaumWelchLearner;
import be.ac.ulg.montefiore.run.jahmm.toolbox.KullbackLeiblerDistanceCalculator;
import be.ac.ulg.montefiore.run.jahmm.toolbox.MarkovGenerator;

/**
 * Created by Anders on 19-02-2017.
 */

public class HMM
{
 /* Possible packet reception status */

    public enum Packet {
        OK, LOSS;

        public ObservationDiscrete<Packet> observation() {
            return new ObservationDiscrete<Packet>(this);
        }
    };


    static public void main(String[] argv) throws java.io.IOException
    {
  /* Build a HMM and generate observation sequences using this HMM */

        Hmm<ObservationDiscrete<Packet>> hmm = buildHmm();

        List<List<ObservationDiscrete<Packet>>> sequences;
        sequences = generateSequences(hmm);

  /* Baum-Welch learning */

        BaumWelchLearner bwl = new BaumWelchLearner();

        Hmm<ObservationDiscrete<Packet>> learntHmm = buildInitHmm();

        // This object measures the distance between two HMMs
        KullbackLeiblerDistanceCalculator klc = new KullbackLeiblerDistanceCalculator();

        // Incrementally improve the solution
        for (int i = 0; i < 10; i++) {
            System.out.println("Distance at iteration " + i + ": " +
                    klc.distance(learntHmm, hmm));
            learntHmm = bwl.iterate(learntHmm, sequences);
        }

        System.out.println("Resulting HMM:\n" + learntHmm);

  /* Computing the probability of a sequence */

        ObservationDiscrete<Packet> packetOk = Packet.OK.observation();
        ObservationDiscrete<Packet> packetLoss = Packet.LOSS.observation();

        List<ObservationDiscrete<Packet>> testSequence = new ArrayList<ObservationDiscrete<Packet>>();
        testSequence.add(packetOk);
        testSequence.add(packetOk);
        testSequence.add(packetLoss);

        System.out.println("Sequence probability: " +
                learntHmm.probability(testSequence));

  /* Write the final result to a 'dot' (graphviz) file. */

        (new GenericHmmDrawerDot()).write(learntHmm, "learntHmm.dot");
    }


 /* The HMM this example is based on */

    static Hmm<ObservationDiscrete<Packet>> buildHmm()
    {
        Hmm<ObservationDiscrete<Packet>> hmm = new Hmm<ObservationDiscrete<Packet>>(2, new OpdfDiscreteFactory<Packet>(Packet.class));

        hmm.setPi(0, 0.95);
        hmm.setPi(1, 0.05);

        hmm.setOpdf(0, new OpdfDiscrete<Packet>(Packet.class, new double[] { 0.95, 0.05 }));
        hmm.setOpdf(1, new OpdfDiscrete<Packet>(Packet.class, new double[] { 0.20, 0.80 }));

        hmm.setAij(0, 1, 0.05);
        hmm.setAij(0, 0, 0.95);
        hmm.setAij(1, 0, 0.10);
        hmm.setAij(1, 1, 0.90);

        return hmm;
    }


 /* Initial guess for the Baum-Welch algorithm */

    static Hmm<ObservationDiscrete<Packet>> buildInitHmm()
    {
        Hmm<ObservationDiscrete<Packet>> hmm = new Hmm<ObservationDiscrete<Packet>>(2, new OpdfDiscreteFactory<Packet>(Packet.class));

        hmm.setPi(0, 0.50);
        hmm.setPi(1, 0.50);

        hmm.setOpdf(0, new OpdfDiscrete<Packet>(Packet.class,new double[] { 0.8, 0.2 }));
        hmm.setOpdf(1, new OpdfDiscrete<Packet>(Packet.class,new double[] { 0.1, 0.9 }));

        hmm.setAij(0, 1, 0.2);
        hmm.setAij(0, 0, 0.8);
        hmm.setAij(1, 0, 0.2);
        hmm.setAij(1, 1, 0.8);

        return hmm;
    }


 /* Generate several observation sequences using a HMM */

    static <O extends Observation> List<List<O>> generateSequences(Hmm<O> hmm)
    {
        MarkovGenerator<O> mg = new MarkovGenerator<O>(hmm);

        List<List<O>> sequences = new ArrayList<List<O>>();
        for (int i = 0; i < 200; i++)
            sequences.add(mg.observationSequence(100));

        return sequences;
    }
}