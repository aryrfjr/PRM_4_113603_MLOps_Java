from pydantic import BaseModel, Field
from app.schemas import GenericStatusResponse

########################################################################
#
# Pydantic schemas for DataOps request/response
#
########################################################################


class ExtractSoapsRequest(BaseModel):
    cutoff: float = Field(
        ..., ge=0, example=3.75, description="A cutoff for local region in angstroms."
    )
    l_max: int = Field(
        ..., ge=0, example=6, description="The maximum degree of spherical harmonics."
    )
    n_max: int = Field(
        ..., ge=0, example=8, description="The number of radial basis functions."
    )
    n_Z: int = Field(..., ge=0, example=3, description="Number of chemical species.")
    Z: str = Field(
        ...,
        example="{13 29 40}",
        description="The chemical species as a list of atomic numbers.",
    )
    n_species: int = Field(
        ..., ge=0, example=3, description="Number of chemical species."
    )
    Z_species: str = Field(
        ...,
        example="{13 29 40}",
        description="The chemical species as a list of atomic numbers.",
    )


class ExtractSoapsResponse(GenericStatusResponse):
    pass
