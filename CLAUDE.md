# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build (includes spotless formatting)
./gradlew build

# Run tests with JaCoCo coverage
./gradlew test

# Run a single test class
./gradlew test --tests "com.just.goap.AgentTest"

# Run a single test method
./gradlew test --tests "com.just.goap.AgentTest.testMethodName"

# Format code (automatically runs on build)
./gradlew spotlessApply

# Check formatting without applying
./gradlew spotlessCheck
```

## Architecture Overview

Just-GOAP is a Goal-Oriented Action Planning library for AI. The planning algorithm uses AO* search to find optimal action sequences that satisfy goals.

### Core Components

**StateKey** (`StateKey.java`): Sealed class with two subtypes that distinguish how state is sourced:
- `StateKey.Derived<T>`: State produced by action effects
- `StateKey.Sensed<T>`: State obtained from sensors (environment queries)

This distinction enables graph validation - the system verifies that all preconditions can be satisfied either by sensors (for sensed keys) or by action effects (for derived keys).

**Graph** (`graph/Graph.java`): Immutable, stateless container of actions, goals, and sensors. Built once via `Graph.builder()`, validated on build, then shared across all agents. The graph pre-computes which actions satisfy which conditions (`preconditionToSatisfyingActionsMap`).

**Agent** (`Agent.java`): Stateful per-entity wrapper. Holds current plan, world state cache, and tick counter. Call `Agent#update(graph, context)` each tick. Agents can share the same Graph instance.

**AOStar** (`AOStar.java`): AO* search implementation. Finds minimum-cost action sequence by expanding unsatisfied conditions and tracking g-cost (accumulated action costs) and h-cost (heuristic estimate based on cheapest satisfying actions).

### Key Patterns

**Lazy sensor evaluation**: World state (`SensingWorldState`) only queries sensors when state is actually requested during planning. Sensors are not eagerly evaluated.

**Condition/Effect system**: Actions have:
- Preconditions: Conditions that must be true before execution (can reference derived or sensed keys)
- Effects: State changes produced by the action (only derived keys)

Goals have:
- Desired conditions: What the goal wants to achieve (derived keys only)
- Preconditions: What must be true to consider this goal (sensed keys only)

**Graph validation** (`graph/GraphValidator.java`): On build, validates that:
- Every action's preconditions can be satisfied (by sensors or other actions)
- Every goal's conditions can be satisfied
- No orphaned actions or unreachable goals

### Package Structure

- `com.just.goap`: Core types (Agent, Action, Goal, StateKey, AOStar)
- `com.just.goap.condition`: Condition and Expression system for preconditions
- `com.just.goap.effect`: Effect types for action outcomes
- `com.just.goap.graph`: Graph builder and validator
- `com.just.goap.plan`: Plan execution logic
- `com.just.goap.sensor`: Sensor interfaces (Mono, Multi) and composition utilities
- `com.just.goap.state`: World state implementations (WorldState, SensingWorldState, SimulatedWorldState)

## Code Style

Uses Eclipse formatter (`eclipse-formatter.xml`) via Spotless. Key settings:
- 4-space indentation (leading tabs converted to spaces)
- Import order: default, java, com.just, static imports
