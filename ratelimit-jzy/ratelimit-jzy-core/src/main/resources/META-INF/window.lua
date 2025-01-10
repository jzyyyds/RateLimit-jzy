--获取key
local key = KEYS[1];
--获取参数
--获取窗口的容量
local capacity = tonumber(ARGV[1])
--获取窗口的时间
local time = tonumber(ARGV[2])
--获取当前的时间
local now = tonumber(ARGV[3])
--获取唯一标识
local requestId = tonumber(ARGV[4])

--移除过期的数据
redis.call('zremrangebyscore',key,0,now-time)
--获取集合中存在的数据
local ready_request = redis.call('zcard',key)
--判断
local isAllow = 0
if ready_request < capacity then
    --加入数据
    redis.call('zadd',key,now,requestId)
    isAllow = 1
end
--设置过期时间
redis.call('expire',key,time+1)
return isAllow
