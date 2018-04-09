package com.batalov.RL;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;

import com.batalov.RL.QLearningOrchestrator.BooleanInput;
import com.batalov.RL.QLearningOrchestrator.BooleanInputDescriptor;
import com.batalov.RL.QLearningOrchestrator.FixedPointInput;
import com.batalov.RL.QLearningOrchestrator.FixedPointInputDescriptor;
import com.batalov.RL.QLearningOrchestrator.InputsQState;

public class QLearningOrchestratorTest {

	@Test
	public void testInputsQStateHashCode() {
		final Map<String, BooleanInputDescriptor> descriptors = new HashMap<String, BooleanInputDescriptor>();
		final Map<String, BooleanInput> inputs = new TreeMap<String, BooleanInput>();
		for (int r = 0; r < 3; r++) {
			for (int c = 0; c < 3; c++) {
				final String name = r + "-" + c;
				descriptors.put(name, BooleanInputDescriptor.INSTANCE);
				inputs.put(name, new BooleanInput(false));
			}
		}
		inputs.put("0-0", new BooleanInput(true));
		inputs.put("0-2", new BooleanInput(true));
		inputs.put("1-1", new BooleanInput(true));
		inputs.put("2-2", new BooleanInput(true));
		final InputsQState qs = new InputsQState(descriptors, inputs, null);
		
		System.out.println(qs.hashCode());
	}

//	@Test
	public void testFixedPointRepresentableInFloat() {
		boolean issue = false;
		int power10 = 1;
		int maxF = 8;
		int maxInt = (int) Math.pow(10, maxF-1);
		System.out.println("range " + (-maxInt) + ".." + maxInt);
		for (int f = 0; f < maxF; f++) {
			System.out.println("f = " + f);
			for (int i = -maxInt; i < maxInt; i++) {
				/*
				float power10 = (float)Math.pow(10.0, f);
				final float fl = i/power10;
				*/
				float fl = Float.valueOf(f > 0 ? String.format("%s%d.%0" + f + "d", (i < 0 ? "-" : ""), Math.abs(i/power10), Math.abs(i%power10)) : String.format("%d.", i));
				String fls = String.format("%." + f + "f", fl);
				int r = Integer.valueOf(fls.replace(".", ""));
//				float prf = fl*power10;
				float prf  = 0f;
//				int r = Math.round(prf);
//				assert r == i : "Issue int = " + i + ", f = " + f + ", pow = " + power10 + ", prf = " + prf + ", r = " + r + ", float = " + fl;

				if (r != i) {
					System.out.println("Issue int = " + i + ", f = " + f + ", pow = " + power10 + ", prf = " + prf + ", r = " + r + ", float = " + fl);
					issue = true;
				}
			}
			power10 *= 10;
		}
		assertFalse(issue);
	}
	
	@Test
	public void testFixedPointInput() {
		final FixedPointInputDescriptor fpid1 = FixedPointInputDescriptor.newWith(0f, 1f, 1);
		final FixedPointInput fpi1 = new FixedPointInput(0.378f, 1);
		assertEquals(fpi1.toString(), "0.4");
		assertEquals(fpi1.getValue(), new Float(0.4f));
		assertTrue(fpid1.matches(fpi1));

		// take precision into account when matching
		assertFalse(fpid1.matches(new FixedPointInput((Float)fpi1.getValue(), 2)));
		assertFalse(FixedPointInputDescriptor.newWith(0f, 1f, 2).matches(fpi1));

		final FixedPointInputDescriptor fpid2 = FixedPointInputDescriptor.newWith(-16f, -15f, 0);
		final FixedPointInput fpi2 = new FixedPointInput(-15.98f, 0);
		assertEquals(fpi2.toString(), "-16.");
		assertEquals(fpi2.getValue(), new Float(-16f));
		assertTrue(fpid2.matches(fpi2));
		
		assertFalse(fpid1.matches(fpi2));
		assertFalse(fpid2.matches(fpi1));
	}
}
