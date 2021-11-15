--[[
	Performs MQTT setup.
]]



local client_id = "Nico"

local broker_url = "tcp://broker.emqx.io:"
local broker_port = 1883

-- set default connection options
--mqtt.connOpts:setCleanSession(true)



-- SETTER
--if not mqtt.firstRun then
--	mqtt:reset()
--end

mqtt:setClientId(client_id)
mqtt:setClientBroker(broker_url..broker_port)

mqtt.firstRun = false
