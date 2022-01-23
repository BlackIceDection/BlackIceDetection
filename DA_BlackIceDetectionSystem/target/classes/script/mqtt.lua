--[[
	Performs MQTT setup.
]]



local client_id = "Nico"
local topic1 = "temperature"
local topic2 = "thermal"

local broker_con_type = "tcp"
local broker_url = "192.168.43.4"
local broker_port = 1883
local qos = 0

-- set default connection options
--mqtt.connOpts:setCleanSession(true)



-- SETTER
if not mqtt.firstRun then
	mqtt:reset()
end

mqtt:setClientId(client_id)
mqtt:setClientBroker(broker_con_type .."://"..broker_url..":"..broker_port)
mqtt:setQos(qos)
mqtt:setTopic1(topic1)
mqtt:setTopic2(topic2)

mqtt.firstRun = false
