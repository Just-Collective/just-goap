# Just-GOAP
Just's implementation of GOAP, an effective approach to AI with the goal of creating dynamic/emergent behavior as seen in games such as F.E.A.R.

## What is GOAP?
GOAP (Goal-Oriented Action Planning) is a strategy for AI problem solving, similar to other strategies such as behavior trees, FSMs (Finite State Machines), utility AI, and so on. Instead of manually linking nodes as one might do in a behavior tree or FSM, GOAP works by providing a set of actions and goals and letting the AI create a plan (using the provided pool of actions) to solve a certain goal. Plan creation is accomplished by using pathfinding (typically A* or AO*) algorithms to find an optimal "path" (chain of actions) that completes the target goal.

## How does this implementation of GOAP work?

### World State
As all GOAP implementations do, Just's GOAP implementation maintains a world state map of state keys to state data. States can be any data you want - from primitives to complex objects. Just-GOAP was designed with liberty of state data in mind. The only exposure to world state is through action cost computation or action execution.

### State Keys
State keys are objects that have a simple String id associated with them. There are two types of keys: `Derived` keys and `Sensed` keys.

- `Derived` keys represent state which is derived from action execution. Only `Derived` keys can be used in action effects.
- `Sensed` keys represent state that is sensed from the agent's environment. Only `Sensed` keys can be used in sensors.

It is ***VERY IMPORTANT*** that the difference between these two types of keys is understood, as they are important for graph validation (see graph creation and validation sections). Which key type you choose is dependent on how you will use the key. If the key represents state that is going to be sensed from the world, then you most certainly want a `Sensed` key. If the key represents state that is computed only from an action, then use a `Derived` key.

If you happen to need a key that is both `Derived` and `Sensed`, please note that you can freely convert between the key types at any time using the `Derived#asSensed` and `Sensed#asDerived` methods. There is no penalties or gotchas for doing so as an end developer, the key types are mostly to help with graph validation (and also to help with reasoning about how your GOAP agent will handle state).

### Sensors
Just-GOAP has sensor system that is used to extract external state into a world state map for GOAP to use in planning. Most GOAP agents will almost always have sensors, as an agent can not do much without any information from its surroundings. Sensors are where you'll want to do world scans such as nearby entities, the agent's current inventory state, held items, and so on.

One important feature of Just-GOAP's sensor system is that sensors are evaluated **lazily**. An agent will only request external information via sensors as it needs world state information during planning. This is ideal if some of your sensors are expensive, or if you have many sensors. By evaluating sensed state lazily, Just-GOAP avoids eager, wasteful world state observations.

Another important feature of Just-GOAP's sensor system is that you can create derived sensors. Suppose you want to scan all entities near your agent. Then suppose that you want to filter down those scanned agents so that you can avoid a rescan. You can achieve this by first creating a `Direct` sensor of nearby entities that performs the world scan, and then create a `Derived` sensor which takes the key of the nearby entities sensor and filters on the provided collection with some predicate.

### Conditions and Expressions
Actions support preconditions. Preconditions may either be derived or sensed. This means that either sensors can be used to satisfy an action's preconditions, or other actions can satisfy an action's preconditions, allowing for actions to be chained (which is essential for creating plans). **It is recommended that your "initiating" actions have either no preconditions or only sensed preconditions, and your intermediate actions have derived preconditions with minimal sensed preconditions, ideally 0.**

Goals support both preconditions and desired conditions. Preconditions are useful on goals if you want to prune a goal from running early based on simple state. Goal preconditions require sensed keys, so make sure you have sensors for goal preconditions.

### Graph Creation
Graph creation uses a builder pattern, with which actions and goals can be submitted prior to building. For a given AI agent, a graph only needs to be created **once**. This means that graphs are statically built and can not be dynamically modified post-build. There were several reasons for this design choice, the most important of which is graph validation (see graph validation section).

### Graph Validation
When a graph is built, the graph is validated prior to the build method returning. Statically built graphs enables many reasonable assumptions to be made, making validation thorough and powerful at stopping unexpected or missing AI behavior before it happens. This is a small subset of what gets validated:

- Can an action satisfy another action?
- Can an action satisfy a goal?
- Can the preconditions of an action be satisfied?
- Can the preconditions of a goal be satisfied?
- etc.

### Agent Instancing
While the graph of actions/goals is created only once, you can create any number of agents that you plan to support and have them use the same exact graph. Graphs are stateless, immutable data, so all agents may freely reference the same graph without fear of conflicting state or concurrency issues. Should your agent require some form of state, it is recommended that your GOAP agent be merely a component of some overall entity class (which can then be used as a context for the agent).

### Agent Context
GOAP agents take in a "context" object. This context type is open-ended and may be whatever you need to complete actions. Typically the context is some larger "entity" instance that holds external state, but your needs may differ.

### Agent Updating
To run the GOAP AI, you simply need to call Agent#update. The update method takes only a context object.

### Agent Planning
When the GOAP agent runs, it will first try to create a plan given the graph that the agent was provided. This involves pathfinding using an AO* algorithm to determine the best plan to complete the "best goal", where the best goal is whichever goal the agent can satisfy with the least amount of cost (cost is dependent on your actions + heuristics).

If the agent fails to create a plan, it will simply do nothing. This may happen if all of its goals are not reachable (the world state is such that none of the goals can be completed) or if all of the goals are satisfied (they are already completed so there is nothing left to do). It is up to implementers of GOAP to make sure that all of their goals are achievable.

## Best Practices
Like many other systems, GOAP is simply a tool. It is not the definitive answer to all AI problems, nor is it the only answer to all AI problems. In fact, there are some cases where using GOAP can be detrimental compared to some other approach.

One example would be if the graph complexity isn't all that complex (in other words, the agent doesn't have much to do). In that case, there is no reason to use GOAP because the planning overhead will usually be more than the actual execution itself.

Another example of when GOAP would be a poor choice is if your actions are specialized. For example, suppose we have a game where there are many potions the agent can pick up, equip and drink, all to achieve different effects for different goals. In this specific case, there may be multiple types of potions that have the same effects. You will quickly find that it is difficult to generalize actions such that the same-type potions result in the same effects. There are certainly ways to achieve generalized actions, but the boilerplate required to do so may not be ideal to you. In cases like, this it is recommended to **generalize state, not actions**. Instead of having `HAS_FIRE_RESISTANCE_POTION`, `HAS_LAVA_RESISTANCE_POTION`, `HAS_OMNI_POTION` with the goal of giving the agent fire resistance, consolidate these into `HAS_FIRE_RESISTING_ITEM`, and then let actions select the best item from a set using a Utility AI or similar approach.

Due to how Just's implementation of GOAP works, there are actually quite a few neat tricks you can do. For example, it is technically possible to switch graphs on-the-fly. This is perfect if combined with a Finite State Machine. Consider for example if you have different states for an agent, such as "IDLE", "COMBAT" and "AWARE". These states could be mapped to different GOAP graphs so long as each graph alters the world state in such a way that the agent's state can change again later on. This approach allows you to considerably cut down potentially unwanted planning paths and improve performance. Furthermore, you could apply this same technique wherever you use sub-FSMs, allowing you to purge more unwanted planning paths.