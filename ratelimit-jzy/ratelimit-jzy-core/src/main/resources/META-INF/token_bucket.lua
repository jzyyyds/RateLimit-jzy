--获取key
local token_key = KEYS[1];
--获取时间戳的key
local time_key = KEYS[2];
--获取参数
--获取容量
local capacity = tonumber(ARGV[1]);
--获取每秒产生的速率
local rate = tonumber(ARGV[2]);
--获取当前的时间戳
local now = tonumber(ARGV[3]);
redis.log(redis.LOG_WARNING, "now " .. ARGV[3])
--计算过期的时间
local fill_time = capacity/rate;
local expire = math.floor(fill_time * 2)

--获取上一次的令牌桶的令牌个数
local last_request =  tonumber(redis.call('GET',token_key));


if last_request == nil then
    last_request = capacity
end
redis.log(redis.LOG_WARNING, "last_request " .. last_request);
--获取上一次的更新的时间
local last_request_time = tonumber(redis.call('GET',time_key));
if last_request_time == nil then
    last_request_time = 0
end
redis.log(redis.LOG_WARNING, "last_request_time " .. last_request_time);

--计算时间差
local time_difference = math.max(0, now - last_request_time);
redis.log(redis.LOG_WARNING, "time_difference " .. time_difference);
--计算当前桶的令牌数
local now_token = math.min(capacity,last_request + time_difference * rate);
--判断是否允许通过
local is_allow = 0;
local current_token = now_token;
if now_token > 0 then
    --说明可以通过
    is_allow = 1;
    current_token = now_token - 1;
    --更新数据
    redis.call('SETEX', token_key, expire, current_token);
    redis.call('SETEX', time_key, expire, now);
end

return is_allow;
