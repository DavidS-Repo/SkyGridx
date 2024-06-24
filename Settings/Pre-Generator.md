# **Pre-Generator**

Built in async pre-generator feature that allows for efficient world generation. This feature is accessible through the **/pregen** command, with customizable parameters for Parallel Tasks Multiplier, print update delay, world and radius.

Works best on paper servers, on none paper servers the async functionality will not be used. Recommend you go into your paper server paper-global.yml and update these

## Config
```yaml
chunk-system:
  gen-parallelism: default
  io-threads: 12
  worker-threads: 12
```
   - Adjust `io-threads` and `worker-threads` to match your CPU’s thread count. Default settings utilize only half.
   - Usage: /pregen <ParallelTasksMultiplier> <PrintUpdateDelayin(Seconds/Minutes/Hours)> <world> <Radius(Blocks/Chunks/Regions)>

## Command usage
### Examples:
`/pregen 6 5s world 1000b`
- Pre-Generate the `overworld` 
- (threads * 6) results in a max of 72 concurrent parallel_tasks
- Prints logs every 5 seconds
- 1000 block radius, (1000 / 16) = 62.5, rounded to 62 chunks, then squared to ge total chunks, 62x62 = 3844 Chunks that will need to be processed

`/pregen 2 2m world_nether 500c`
- Pre-Generate the `nether`
- (threads * 2) results in a max of 24 concurrent parallel_tasks
- Prints logs every 2 minutes
- 500 chunk radius, squared to ge total chunks, (500 * 500) = 250000 Chunks that will need to be processed

`/pregen 1 12h world_the_end 100r`
- Pre-Generate the `the end`
- (threads * 1) results in a max of 12 concurrent parallel_tasks
- Prints logs every 12 hours
- 100 regions radius, 1 regions is (32 * 32) chunks, to get the radius we multiply (32 * 100) = 3200 squared to ge total chunks, (3200 * 3200) = 10240000 Chunks that will need to be processed

`/pregen 4 10s world default`
- Pre-Generate the `overworld`
- (threads * 4) results in a max of 48 concurrent parallel_tasks
- Prints logs every 10 seconds
- `default` is the stadard world border for a Minecraft world, 14062361500009 total chunks in the world, you can use this if you just want to leave it on for a long time

## Command Settings
### `ParallelTasksMultiplier`
- it is recommended to stay below your thread count.
- limits the number of parallel chunk load tasks. 
- It is multiplied by the number of threads available at server initialization. For instance, if your server starts with 12 threads, the maximum number of parallel tasks allowed when **ParallelTasksMultiplier** is set to 6 will be 72.
- A **ParallelTasksMultiplier** of 6 yielded ~150-200 chunks per second on a 5600x CPU, depending on server activity and other system tasks.
- Increasing **ParallelTasksMultiplier** beyond current CPU utilization can further enhance performance. For example, setting it to 12 yielded ~190-250 chunks per second.
- In summary, **ParallelTasksMultiplier** determines the load on your server. A smaller number results in a lower load and fewer chunks per second, while a larger number increases the server load but improves chunk processing speed.
### `PrintUpdateDelay`
- Used to determine how often you want the logs to be printed
- The supported time frames are Seconds, Minutes, and Hours
- To use it you just have to add the letters s,m, or h next to the actual number 
- For example, **5m** for 5 minutes or **20s** for 20 seconds or **1h** for 1 hour
### `World`
- Determines what world you want to pregenerate
- Tab auto complete will fetch all the vanilla worlds in the server and show them to you
- Then you can choose what world you want and off you go.
### `Radius` 
- Determines the radius of chunks that will be pre-generated
- The supported time frames are Blocks, Chunks and Regions
- To use it you just have to add the letters b,c, or r next to the actual number 
- For example, **20000b** is a 20000 block radius or **500c** is a 500 chunk radius or **30r** is a 30 region radius