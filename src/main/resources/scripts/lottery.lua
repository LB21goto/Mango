-- KEYS[1] = poolKey (List)
local poolKey = KEYS[1]

local prizeId = redis.call('RPOP', poolKey)
if not prizeId then
    return 0
end

return tonumber(prizeId)