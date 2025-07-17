from pydantic import BaseModel, Field, field_validator, model_validator
from typing import List

########################################################################
#
# Pydantic schemas for ModelOps request/response
#
########################################################################

ALLOWED_KERNEL_TYPES = {"SE", "OU", "CUSTOM"}  # TODO: maybe shouldn't be hardcoded


class PBSSDBEvaluationRequest(BaseModel):

    #
    # Attributes
    #
    ########################################################################
    cross_evaluation: bool = Field(
        ...,
        example=True,
        description="Cross evaluation uses use one database for testing and another one for training.",
    )
    nominal_compositions: List[str] = Field(
        ...,
        min_items=1,
        max_items=2,
        example=["Zr49Cu49Al2", "Zr47Cu47Al6"],
        description="The first one is for training and if a second is set, that one will be for testing.",
    )
    pbssdb_versions: List[str] = Field(
        ...,
        min_items=1,
        max_items=2,
        example=["V20250717T105640Z", "V20250717T105809Z"],
        description="The first one is for training and if a second is set, that one will be for testing.",
    )
    specie_a: str = Field(
        ..., example="Cu", description="Chemical element symbol in the chemical bond."
    )
    specie_b: str = Field(
        ..., example="Al", description="Chemical element symbol in the chemical bond."
    )
    training_set_size: int = Field(..., example=2000, description="Training set size.")
    testing_set_size: int = Field(..., example=10000, description="Testing set size.")
    kernel_type: str = Field(
        ...,
        example="CUSTOM",
        description='Gaussian Process Regression Kernel Types: (i) "SE": Squared Exponential Kernel (RBF/Gaussian); '
        '(ii) "OU": Ornstein-Uhlenbeck Kernel (Exponential Kernel); (iii) "CUSTOM" Custom Kernel (SE × SOAP Similarity Average).',
    )
    zeta: float = Field(..., example=1.0, description="Hyperparameter.")
    sigma: float = Field(..., example=1.0, description="THyperparameter.")
    regp: float = Field(..., example=0.04, description="Hyperparameter.")

    #
    # Validators for attributes
    #
    ########################################################################

    @field_validator("kernel_type")
    @classmethod
    def validate_kernel_type(cls, value):
        if value not in ALLOWED_KERNEL_TYPES:
            raise ValueError(
                f"Invalid Gaussian Process Regression Kernel Type: {value}"
            )
        return value

    @model_validator(mode="after")
    def validate_cross_evaluation_and_compositions(self):
        if self.cross_evaluation and len(self.nominal_compositions) != 2:
            raise ValueError(
                f"If cross_evaluation is True, nominal_compositions (len={len(self.nominal_compositions)}) must have two items"
            )
        return self

    @model_validator(mode="after")
    def validate_versions_and_compositions(self):
        if len(self.pbssdb_versions) != len(self.nominal_compositions):
            raise ValueError(
                f"pbssdb_versions (len={len(self.pbssdb_versions)}) must match nominal_compositions (len={len(self.nominal_compositions)})"
            )
        return self
