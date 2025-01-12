--获取key
local key = KEYS[1];
--获取时间戳的key
local time_key = KEYS[2];
--获取桶的容量
local capacity = tonumber(ARGV[1]);
--获取消费的速率
local rate = tonumber(ARGV[2]);
--获取当前的时间
local now = tonumber(ARGV[3]);
--redis.log(redis.LOG_WARNING, "key " .. KEYS[1])
--redis.log(redis.LOG_WARNING, "time_key " .. KEYS[2])
--redis.log(redis.LOG_WARNING, "rate " .. ARGV[2])
--redis.log(redis.LOG_WARNING, "capacity " .. ARGV[1])
redis.log(redis.LOG_WARNING, "now " .. ARGV[3])
--获取数据
--获取当前桶中的数据
local key_bucket_count = tonumber(redis.call('GET',key)) or 0
--获取上次更新的时间
local last_time = tonumber(redis.call('GET',time_key)) or now
-- 键的生命周期
local key_lifetime = math.ceil((capacity / rate) + 1)
--获取时间差
local time_difference = now - last_time;

--计算已经漏出的数据
local request = time_difference * rate;
--计算现在桶中的数量
if request > 0 then
    if request > key_bucket_count then
        key_bucket_count = 0
    else
        key_bucket_count = key_bucket_count - request
    end
end

local isAllow = 0;

if key_bucket_count < capacity then
    isAllow = 1
    --更新数据&设置过期时间
    redis.call('SETEX',key,key_lifetime,key_bucket_count+1);
    redis.call('SETEX',time_key,key_lifetime,now);
end


--返回
return isAllow