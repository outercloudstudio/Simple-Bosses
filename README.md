# Simple Bosses

## Idea
Create bosses and assign the moves. Moves will apply tags that allow command blocks to react and execute commands.

## Commands
`/boss create [selector] [boss name]` Creates a boss with the specified name. You may only specify one entity in the selector.

`/boss remove [boss name]` Removes the boss with the specified name.

`/boss trigger [boss name] [move name]` Manually triggers the specified move on the specified boss. Useful for custom AI or testing. Will override any cooldown or move that is currently running.

`/boss moves [boss name] add [move name] [cooldown] [windup] [duration] [recover]` Create a move on a boss with the specified name.

### Cooldown
The time in seconds after completing this move to wait before allowing any other moves to begin. This stage is not detectable by tags. Use recovery if you need to detect for the end of a move.
### Windup
Time in seconds before the move actually is triggered. Useful for telegraphing attacks. Can be detected with these two tags: `boss_once_windup_[move name]` and `boss_windup_[move name]`. Should be set to 0 if not telegraphing attacks.
### Duration
How long the move lasts. Can be detected with these two tags: `boss_once_move_[move name]` and `boss_move_[move name]`. Set to 0 for one shot attacks or longer for attacks that take time.
### Recover
Time in seconds after the move actually is triggered. Useful for giving brief periods of rest after attacks. Can be detected with these two tags: `boss_once_recover_[move name]` and `boss_recover_[move name]`. Should be set to 0 if not special recovery logic is implemented.

`/boss moves [boss name] edit [move name] [cooldown] [windup] [duration] [recover]` Edits a move on a boss with the specified name.

`/boss moves [boss name] remove [move name]` Removes a move on a boss with the specified name.

`/boss moves [boss name] list` Lists the moves on a specified boss.

## Once vs Normal
The once variation of tags are only on an entity for one single tick when the boss has just entered that stage of the move. This is useful for doing things only once per move. The normal variation of the tags will stay there for the duration of the move. Useful for applying effects that last the whole move.

## Tutorial
1. Spawn an entity
2. Create a boss of that entity
3. Give that entity a move
4. Place an always active repeating command block that executes a command on entities with the tag that can be used to detect that move. Remember, the format is `boss_once_move_[move name]` or `boss_move_[move name]`
