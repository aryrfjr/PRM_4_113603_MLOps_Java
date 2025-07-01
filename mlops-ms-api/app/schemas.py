from pydantic import BaseModel

########################################################################
#
# Pydantic schemas for request/response
#
########################################################################

#
# Miscellaneous
#
########################################################################


class PingResponse(BaseModel):
    message: str
