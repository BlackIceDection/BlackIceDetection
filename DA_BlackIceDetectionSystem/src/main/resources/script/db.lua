--[[
	Performs various startup-related tasks like setting crucial initialization variables.
]]



local url = "http://localhost:"
local port = 8086

local user = "BlackIceDetection"
local pwd = "KaindorfBID"
local access_token = "Tkm3bEG7rJEK9PfCywCTue9rannUmQVKS5uXbn5SHmAqyZIm31qtTSWACFV2ym6TkyxgxxAgP6NOVa5rstAk8A=="
local bucket = "BidBucket"
local org = "Kaindorf"



-- SETTER
-- if this script already ran at least once, perform a reset before initializing
if not database.firstRun then
	database:reset()
end

database:setUrl(url..port)
database:setUsername(user)
database:setPassword(pwd)
database:setToken(access_token)
database:setBucket(bucket)
database:setOrg(org)

database.firstRun = false
