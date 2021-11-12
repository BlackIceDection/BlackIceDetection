--[[
	Performs MQTT setup.
]]

local client_id = "Nico"

local broker_url = "tcp://broker.emqx.io:"
local broker_port = 1883



-- GETTER & SETTER
mqtt:setClientId(client_id)
mqtt:setClientBroker(broker_url..broker_port)
