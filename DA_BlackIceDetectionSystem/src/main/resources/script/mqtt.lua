--[[
	Performs MQTT setup.
]]



local client_id = "Nico"
local topic = "Schwarzi"

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
mqtt:setTopic(topic)

mqtt.firstRun = false
