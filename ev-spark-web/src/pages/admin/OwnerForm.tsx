import { useForm, useFieldArray } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Button } from "@/components/ui/button";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";

interface Vehicle {
  make: string;
  model: string;
  licensePlate: string;
  year: number;
}

interface EVOwner {
  id: string;
  nic: string;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  password?: string;
  vehicleDetails: Vehicle[];
  registeredDate?: string;
  active?: boolean;
}

// ------------------ Zod Schemas ------------------

const vehicleSchema = z.object({
  make: z.string().min(1, { message: "Make is required" }),
  model: z.string().min(1, { message: "Model is required" }),
  licensePlate: z.string().min(1, { message: "License plate is required" }),
  year: z.coerce
    .number()
    .min(1900, { message: "Enter a valid year" })
    .max(new Date().getFullYear() + 1, {
      message: "Year cannot be in the future",
    }),
});

// Schema for creating new owner (with password)
const createOwnerSchema = z.object({
  nic: z.string().min(1, { message: "NIC is required" }),
  firstName: z.string().min(1, { message: "First name is required" }),
  lastName: z.string().min(1, { message: "Last name is required" }),
  email: z.string().email({ message: "Please enter a valid email address" }),
  phoneNumber: z.string().min(1, { message: "Phone number is required" }),
  password: z
    .string()
    .min(6, { message: "Password must be at least 6 characters" }),
  vehicleDetails: z
    .array(vehicleSchema)
    .min(1, { message: "At least one vehicle is required" }),
});

// Schema for updating owner (password optional)
const updateOwnerSchema = z.object({
  nic: z.string().min(1, { message: "NIC is required" }),
  firstName: z.string().min(1, { message: "First name is required" }),
  lastName: z.string().min(1, { message: "Last name is required" }),
  email: z.string().email({ message: "Please enter a valid email address" }),
  phoneNumber: z.string().min(1, { message: "Phone number is required" }),
  password: z
    .string()
    .min(6, { message: "Password must be at least 6 characters" })
    .optional()
    .or(z.literal("")),
  vehicleDetails: z
    .array(vehicleSchema)
    .min(1, { message: "At least one vehicle is required" }),
});

type CreateOwnerFormValues = z.infer<typeof createOwnerSchema>;
type UpdateOwnerFormValues = z.infer<typeof updateOwnerSchema>;
type OwnerFormValues = CreateOwnerFormValues | UpdateOwnerFormValues;

// ------------------ Props ------------------

interface OwnerFormProps {
  owner: EVOwner | null;
  onSubmit: (data: Partial<EVOwner>) => void;
  onCancel: () => void;
}

// ------------------ Component ------------------

export function OwnerForm({ owner, onSubmit, onCancel }: OwnerFormProps) {
  const isEditMode = !!owner;

  const defaultValues: OwnerFormValues = owner
    ? {
        nic: owner.nic || "",
        firstName: owner.firstName || "",
        lastName: owner.lastName || "",
        email: owner.email || "",
        phoneNumber: owner.phoneNumber || "",
        password: "", // Empty for edits
        vehicleDetails:
          owner.vehicleDetails?.length > 0
            ? owner.vehicleDetails
            : [
                {
                  make: "",
                  model: "",
                  licensePlate: "",
                  year: new Date().getFullYear(),
                },
              ],
      }
    : {
        nic: "",
        firstName: "",
        lastName: "",
        email: "",
        phoneNumber: "",
        password: "",
        vehicleDetails: [
          {
            make: "",
            model: "",
            licensePlate: "",
            year: new Date().getFullYear(),
          },
        ],
      };

  const form = useForm<OwnerFormValues>({
    resolver: zodResolver(isEditMode ? updateOwnerSchema : createOwnerSchema),
    defaultValues: defaultValues as OwnerFormValues,
    mode: "onChange", // This will provide better feedback
  });

  const { fields, append, remove } = useFieldArray({
    control: form.control,
    name: "vehicleDetails",
  });

  const handleSubmit = (values: OwnerFormValues) => {
    console.log("Form submitted with values:", values);

    // For updates, remove password if it's empty
    if (isEditMode) {
      const submitData = { ...values };
      if (!submitData.password) {
        delete submitData.password;
      }
      console.log("Submitting update data:", submitData);
      onSubmit(submitData);
    } else {
      console.log("Submitting create data:", values);
      onSubmit(values);
    }
  };

  // Log form errors for debugging
  console.log("Form errors:", form.formState.errors);
  console.log("Is form valid?", form.formState.isValid);

  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(handleSubmit)} className="space-y-4">
        {/* Basic Info */}
        <FormField
          control={form.control}
          name="nic"
          render={({ field }) => (
            <FormItem>
              <FormLabel>NIC</FormLabel>
              <FormControl>
                <Input placeholder="Enter NIC number" {...field} />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <FormField
            control={form.control}
            name="firstName"
            render={({ field }) => (
              <FormItem>
                <FormLabel>First Name</FormLabel>
                <FormControl>
                  <Input placeholder="First name" {...field} />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />

          <FormField
            control={form.control}
            name="lastName"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Last Name</FormLabel>
                <FormControl>
                  <Input placeholder="Last name" {...field} />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />
        </div>

        <FormField
          control={form.control}
          name="email"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Email</FormLabel>
              <FormControl>
                <Input type="email" placeholder="Email address" {...field} />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        <FormField
          control={form.control}
          name="phoneNumber"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Phone Number</FormLabel>
              <FormControl>
                <Input placeholder="Phone number" {...field} />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        {/* Password field - show for create, optional for edit */}
        <FormField
          control={form.control}
          name="password"
          render={({ field }) => (
            <FormItem>
              <FormLabel>
                Password{" "}
                {isEditMode && "(Leave blank to keep current password)"}
              </FormLabel>
              <FormControl>
                <Input
                  type="password"
                  placeholder={
                    isEditMode
                      ? "Enter new password (optional)"
                      : "Enter password"
                  }
                  {...field}
                />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        {/* Vehicle Details */}
        <div className="space-y-3">
          <h3 className="text-lg font-semibold">Vehicle Details</h3>

          {fields.map((field, index) => (
            <div
              key={field.id}
              className="border p-4 rounded-md space-y-2 relative"
            >
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <FormField
                  control={form.control}
                  name={`vehicleDetails.${index}.make`}
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Make</FormLabel>
                      <FormControl>
                        <Input placeholder="Vehicle make" {...field} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                <FormField
                  control={form.control}
                  name={`vehicleDetails.${index}.model`}
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Model</FormLabel>
                      <FormControl>
                        <Input placeholder="Vehicle model" {...field} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                <FormField
                  control={form.control}
                  name={`vehicleDetails.${index}.licensePlate`}
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>License Plate</FormLabel>
                      <FormControl>
                        <Input placeholder="License plate" {...field} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                <FormField
                  control={form.control}
                  name={`vehicleDetails.${index}.year`}
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Year</FormLabel>
                      <FormControl>
                        <Input type="number" min="1900" max="2100" {...field} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>

              {fields.length > 1 && (
                <Button
                  type="button"
                  variant="destructive"
                  onClick={() => remove(index)}
                  className="mt-2"
                >
                  Remove Vehicle
                </Button>
              )}
            </div>
          ))}

          <Button
            type="button"
            variant="outline"
            onClick={() =>
              append({
                make: "",
                model: "",
                licensePlate: "",
                year: new Date().getFullYear(),
              })
            }
          >
            + Add Vehicle
          </Button>
        </div>

        {/* Action Buttons */}
        <div className="flex justify-end space-x-2 pt-4">
          <Button type="button" variant="outline" onClick={onCancel}>
            Cancel
          </Button>
          <Button
            type="submit"
            disabled={!form.formState.isValid || form.formState.isSubmitting}
          >
            {form.formState.isSubmitting
              ? "Saving..."
              : owner
              ? "Update Owner"
              : "Create Owner"}
          </Button>
        </div>
      </form>
    </Form>
  );
}
