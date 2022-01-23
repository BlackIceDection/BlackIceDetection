--[[
	Initial setup for server-side InfluxDB.
]]





-- The URL to InfluxDB, without port
local url = "http://localhost:"
-- The InfluxDB port to use
local port = 8086

-- Username for login
local user = "BlackIceDetection"
-- Password for login
local pwd = "KaindorfBID"
-- The organization of the above user
local org = "Kaindorf"

-- The private access token for InfluxDB, unencrypted
local access_token = "Tkm3bEG7rJEK9PfCywCTue9rannUmQVKS5uXbn5SHmAqyZIm31qtTSWACFV2ym6TkyxgxxAgP6NOVa5rstAk8A=="
-- The Influx-bucket
local bucket = "BidBucket"
-- The Influx measurement (yes, we only have/need 1)
local measurement = "black_ice"





-- SETTER
-- If this script already ran at least once, perform a reset before initializing
if not database.firstRun then
	database:reset() -- If so, reset connections beforehand
end

database:setUrl(url..port) -- Assemble the URL and the port via Lua, then pass the full URL to Java
database:setUsername(user)
database:setPassword(pwd)
database:setToken(access_token)
database:setBucket(bucket)
database:setOrg(org)
database:setMeasurement(measurement)

database.firstRun = false
