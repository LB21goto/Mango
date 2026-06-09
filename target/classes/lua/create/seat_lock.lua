-- KEYS[1] = seat_bitmap
-- KEYS[2] = seat_hash
-- KEYS[3] = stock（可选）

-- ARGV[1] = seatId
-- ARGV[2] = userId
-- ARGV[3] = orderNo
-- ARGV[4] = timestamp
-- ARGV[5] = operationType (LOCK/UNLOCK)

local seatId = tonumber(ARGV[1])

-- ✅ 1. 用 SETBIT 判断 + 锁定（关键优化点）
local old = redis.call('setbit', KEYS[1], seatId, 1)

-- 如果原来就是1，说明被抢了
if old == 1 then
    return 0
end

-- ✅ 2. 扣库存（可选）
if KEYS[3] ~= nil then
    local stock = tonumber(redis.call('get', KEYS[3]) or "0")
    if stock <= 0 then
        -- 回滚 bitmap
        redis.call('setbit', KEYS[1], seatId, 0)
        return 0
    end
    redis.call('decr', KEYS[3])
end

-- ✅ 3. 写 Hash（存附加信息）
redis.call('hset', KEYS[2], seatId, ARGV[2] .. ":" .. ARGV[3])

-- ✅ 4. 记录原始凭证到队列（新增）
local logEntry = ARGV[1] .. ":" .. ARGV[2] .. ":" .. ARGV[3] .. ":" .. ARGV[4] .. ":" .. ARGV[5]
redis.call('rpush', KEYS[4], logEntry)

return 1


---- KEYS[1] = stock
---- KEYS[2] = lock_set
---- KEYS[3] = sold_set
---- ARGV[1] = seatId
--
--if tonumber(redis.call('get', KEYS[1])) <= 0 then
--    return 0
--end
--
--if redis.call('sismember', KEYS[2], ARGV[1]) == 1 then
--    return 0
--end
--
--if redis.call('sismember', KEYS[3], ARGV[1]) == 1 then
--    return 0
--end
--
--redis.call('decr', KEYS[1])
--redis.call('sadd', KEYS[2], ARGV[1])
--return 1

