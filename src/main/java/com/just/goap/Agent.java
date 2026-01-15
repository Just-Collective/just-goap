package com.just.goap;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.List;

import com.just.goap.graph.Graph;
import com.just.goap.plan.DefaultPlanFactory;
import com.just.goap.plan.Plan;
import com.just.goap.plan.executor.PlanExecutor;
import com.just.goap.plan.executor.impl.BestPlanExecutor;
import com.just.goap.state.Blackboard;
import com.just.goap.state.SensingWorldState;
import com.just.goap.state.WorldState;

public final class Agent<T> {

    public static <T> Agent<T> create() {
        return Agent.<T>builder().build();
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    private final Blackboard blackboard;

    private final Blackboard graphBlackboard;

    private final PlanExecutor<T> planExecutor;

    private final PlanFactory<T> planFactory;

    private final WorldState previousWorldState;

    private final Debugger debugger;

    private @Nullable SensingWorldState<T> currentWorldState;

    private long tick;

    private Agent(PlanExecutor<T> planExecutor, PlanFactory<T> planFactory) {
        this.planExecutor = planExecutor;
        this.planFactory = planFactory;
        this.previousWorldState = WorldState.create();
        this.blackboard = new Blackboard();
        this.debugger = new Debugger(this);
        this.graphBlackboard = new Blackboard();

        this.currentWorldState = null;
        this.tick = 0;
    }

    public void update(Graph<T> graph, T actor) {
        debugger.push("Agent.update()");

        prepareWorldStates(graph, actor);
        supplyPlansIfNeeded(graph, actor);
        executePlans(actor);
        tick++;

        debugger.pop();
    }

    public void abandonPlan() {
        planExecutor.abandonAllPlans();
    }

    public boolean hasPlan() {
        return planExecutor.hasActivePlans();
    }

    public Blackboard getBlackboard() {
        return blackboard;
    }

    public Blackboard getGraphBlackboard() {
        return graphBlackboard;
    }

    public long getTick() {
        return tick;
    }

    public Debugger getDebugger() {
        return debugger;
    }

    /**
     * Returns the plan executor for advanced use cases.
     */
    public PlanExecutor<T> getPlanExecutor() {
        return planExecutor;
    }

    private void prepareWorldStates(Graph<T> graph, T actor) {
        if (currentWorldState == null || currentWorldState.getGraph() != graph) {
            // Create a new world state if the current world state is null or the graph has changed.
            this.currentWorldState = new SensingWorldState<>(graph);
            // Clear the graph blackboard for the initial sensor world state or if the graph changed.
            graphBlackboard.clear();
        }

        // Always update the actor here.
        currentWorldState.setActor(actor);
        // Clear the previous world state.
        previousWorldState.clear();
        // Set the previous world state's contents to the current world state's contents.
        previousWorldState.setAll(currentWorldState.getMap());
        // Clear current world state before we use it.
        currentWorldState.clear();
    }

    private void supplyPlansIfNeeded(Graph<T> graph, T actor) {
        if (planExecutor.needsPlans()) {
            debugger.push("Agent.supplyPlans()");

            var plans = planFactory.create(graph, actor, currentWorldState, debugger);
            planExecutor.supplyPlans(plans);

            debugger.pop();
        }
    }

    private void executePlans(T actor) {
        debugger.push("Agent.executePlans()");

        var context = new PlanExecutor.ExecutionContext<>(
            this,
            actor,
            currentWorldState,
            previousWorldState
        );

        planExecutor.execute(context);

        debugger.pop();
    }

    public interface PlanFactory<T> {

        List<Plan<T>> create(Graph<T> graph, T actor, SensingWorldState<T> worldState, Debugger debugger);
    }

    public static class Builder<T> {

        private PlanExecutor<T> planExecutor;

        private PlanFactory<T> planFactory;

        private Builder() {
            this.planExecutor = new BestPlanExecutor<>();
            this.planFactory = DefaultPlanFactory::create;
        }

        public Builder<T> withPlanExecutor(PlanExecutor<T> planExecutor) {
            this.planExecutor = planExecutor;
            return this;
        }

        public Builder<T> withPlanFactory(PlanFactory<T> planFactory) {
            this.planFactory = planFactory;
            return this;
        }

        public Agent<T> build() {
            return new Agent<>(planExecutor, planFactory);
        }
    }

    public static final class Debugger {

        private static final Logger LOGGER = LoggerFactory.getLogger(Debugger.class);

        private final Agent<?> agent;

        private final ArrayDeque<Frame> frameStack;

        private boolean enabled;

        public Debugger(Agent<?> agent) {
            this.agent = agent;
            this.frameStack = new ArrayDeque<>();
            this.enabled = false;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public void push(String name) {
            if (!isEnabled()) {
                return;
            }

            frameStack.push(new Frame(name, System.nanoTime()));
        }

        public void pop() {
            if (!isEnabled()) {
                return;
            }

            if (frameStack.isEmpty()) {
                return;
            }

            var frame = frameStack.pop();
            var timeMs = (System.nanoTime() - frame.startTime) / 1_000_000.0;

            if (enabled) {
                LOGGER.debug("T={}, {} completed in {}ms", agent.getTick(), frame.name, timeMs);
            }
        }

        private record Frame(
            String name,
            long startTime
        ) {}
    }
}
