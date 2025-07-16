from pydantic import BaseModel, Field, conint
from typing import List

########################################################################
#
# Pydantic schemas for DataOps request/response
#
########################################################################


class RunWithSubRunNumbers(BaseModel):
    run_number: int = Field(
        ..., ge=0, example=1, description="Run number (not ID; e.g., 1)."
    )
    sub_run_numbers: List[conint(ge=0, le=14)] = Field(
        ..., example=[0, 1, 2], description="sub-Run (not ID; between 0 and 14)"
    )


class CreatePBSSDBRequest(BaseModel):
    all_runs_with_sub_runs: List[RunWithSubRunNumbers]


class ExtractSOAPVectorsRequest(BaseModel):
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
