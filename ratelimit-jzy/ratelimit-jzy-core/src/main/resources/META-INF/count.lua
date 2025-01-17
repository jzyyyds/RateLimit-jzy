--获取key的值
local key = KEYS[1];
--获取数量
local count = tonumber(ARGV[1]);
--获取过期时间
local time = tonumber(ARGV[2]);
--根据key获取值
local current = redis.call('get', key);
local is_allow = 0;
--判断值是否存在或者当前值是否大于阈值
if current and tonumber(current) >= count then
    --直接返回
    return is_allow
end
--自增
current = redis.call('incr',key);
--设置过期时间
if tonumber(current) == 1 then
    redis.call('expire',key,time)
end

is_allow = 1
--返回
return is_allow;